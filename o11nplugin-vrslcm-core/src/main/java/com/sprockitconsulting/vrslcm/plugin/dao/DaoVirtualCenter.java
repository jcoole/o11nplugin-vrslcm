package com.sprockitconsulting.vrslcm.plugin.dao;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Datacenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.sprockitconsulting.vrslcm.plugin.scriptable.VirtualCenter;
import com.sprockitconsulting.vrslcm.plugin.services.DatacenterService;

@Repository
public class DaoVirtualCenter extends DaoAbstract<VirtualCenter> {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(DaoDatacenter.class);
	
	@Autowired
	public DatacenterService datacenterService;
	
	public DaoVirtualCenter() {
		super();
		setGetAllUrl("/lcm/lcops/api/v2/datacenters/{dcId}/vcenters");
		setGetByValueUrl("/lcm/lcops/api/v2/datacenters/{dcId}/vcenters/{vcName}");
		log.debug("DAO VirtualCenter initialized");
	}

	public List<VirtualCenter> findAllForDatacenter(Datacenter dc) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("dcId", dc.getResourceId());
		return Arrays.asList(doApiRequest(dc.getConnection(), "GET", URL_GET_ALL, "{}", VirtualCenter[].class, uriVariables) );
	}

	@Override
	public List<VirtualCenter> findAll(Connection connection) {
		// Go through all Datacenters in a given connection, return all vCenters.
		List<VirtualCenter> vcs = new ArrayList<VirtualCenter>(0);
		for (Datacenter dc: datacenterService.getAll(connection)) {
			try {
				log.debug("daoVC.findAll() - DC "+dc.getName()+"-"+dc.getResourceId());
				List<VirtualCenter> currentDcVcList = findAllForDatacenter(dc);
				log.debug("daoVC.findAll() - found ["+currentDcVcList.size()+"]");
				if(currentDcVcList.size() > 0) {
					log.debug("daoVC.findAll() - Adding ["+currentDcVcList.size()+"] vCenters to Datacenter ["+dc.getName()+"] list.");
					
					// Assign the connection and Datacenter to each vCenter object
					for(VirtualCenter vc : currentDcVcList) {
						assignConnectionToObject(connection, vc);
						vc.setDatacenter(dc);
					}
					
					vcs.addAll(currentDcVcList);
				} else {
					log.debug("daoVC.findall() - No vCenters found in Datacenter ["+dc.getName()+"].");
				}
			} catch (Exception e) {
				log.error("There was an error adding the VirtualCenters in Datacenter ["+dc.getName()+"] to the list : "+e.getMessage());
				e.printStackTrace();
			}
		}
		return vcs;
	}
	/**
	 * For a given connection, filter out a vCenter connection by name.
	 * @param connection The connection
	 * @param name Name of the vCenter to find
	 * @return VirtualCenter
	 */
	public VirtualCenter findByName(Connection connection, String name) {
		List<VirtualCenter> vcs = findAll(connection);
		log.debug("findByName() - called findAll and got "+vcs.size()+" results to filter for name "+name);
		VirtualCenter vc = vcs.stream()
				.filter(v -> name.equals(v.getName()))
				.findFirst()
				.orElse(null);
		log.debug("findByName() - return result is: "+vc.toString());
		return vc;
	}
	/**
	 * Requests that a vCenter connection be deleted.
	 * @param dc The Datacenter that holds the vCenter connection.
	 * @param name Name of the vCenter to delete
	 * @return Request for deletion
	 */
	public Request delete(Datacenter dc, String name) {
		// Use the DC and name fields for the api call
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("dcId", dc.getResourceId());
		uriVariables.put("vcName", name);
		
		Request deleteRequest = doApiRequest(dc.getConnection(), "DELETE", URL_GET_BY_VALUE, "{}", Request.class, uriVariables);
		assignConnectionToObject(dc.getConnection(), deleteRequest);
		return deleteRequest;
	}


	public Request update(Datacenter dc, String name, VirtualCenter updatedVc) {
		// Use dcId and name in the API call
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("dcId", dc.getResourceId());
		uriVariables.put("vcName", name);
		
		String updateBody = null;
		try {
			updateBody = vroObjectMapper.writeValueAsString(updatedVc);
		} catch (JsonProcessingException e) {
			log.error("There was an error reading the updated vCenter information for the update request: "+e.getMessage());
			e.printStackTrace();
		}
		
		Request updateRequest = doApiRequest(dc.getConnection(), "PUT", URL_GET_BY_VALUE, updateBody, Request.class, uriVariables);
		assignConnectionToObject(dc.getConnection(), updateRequest);
		return updateRequest;
	}


	public Request create(Datacenter dc, VirtualCenter vc) {
		// Use the dc / vc in the call
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("dcId", dc.getResourceId());
		
		String createBody = null;
		try {
			createBody = vroObjectMapper.writeValueAsString(vc);
		} catch (JsonProcessingException e) {
			log.error("There was an error reading the vCenter information for the create request: "+e.getMessage());
			e.printStackTrace();
		}
		Request createRequest = doApiRequest(dc.getConnection(), "POST", URL_GET_ALL, createBody, Request.class, uriVariables);
		assignConnectionToObject(dc.getConnection(), createRequest);
		return createRequest;
	}
	
	public Request sync(Datacenter dc, String name) {
		// Use dcId and name in the API call
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("dcId", dc.getResourceId());
		uriVariables.put("vcName", name);
		
		String dataCollectionUrl = URL_GET_BY_VALUE+"/data-collection";
		
		Request syncRequest = doApiRequest(dc.getConnection(), "POST", dataCollectionUrl, "{}", Request.class, uriVariables);
		assignConnectionToObject(dc.getConnection(), syncRequest);
		return syncRequest;
	}

	// This method is unused.
	@Override
	public VirtualCenter findById(Connection connection, String id) {
		// TODO Auto-generated method stub
		return null;
	}

}
