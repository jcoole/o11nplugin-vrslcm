package com.sprockitconsulting.vrslcm.plugin.dao;

import java.util.List;
import java.util.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Datacenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.sprockitconsulting.vrslcm.plugin.scriptable.VirtualCenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.LockerReference;
import com.sprockitconsulting.vrslcm.plugin.services.CredentialService;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
/**
 * This class contains the data access and manipulation methods for the Datacenter Service.
 * @author justin
 */
@Repository
public class DaoDatacenter 
	extends DaoAbstract<Datacenter> 
	implements IDaoGeneric<Datacenter>, IDaoCreate<Datacenter>, IDaoUpdate<Datacenter>, IDaoDelete<Datacenter> {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(DaoDatacenter.class);
	
	// Additional URL fields
	private String URL_GET_VC = "/lcm/lcops/api/v2/datacenters/{id}/vcenters";
	private String URL_GET_ENV = "/lcm/lcops/api/v2/datacenters/{id}/environments";
	
	@Autowired
	private CredentialService credentialService;
	
	public DaoDatacenter() {
		super();
		setGetAllUrl("/lcm/lcops/api/v2/datacenters");
		setGetByValueUrl("/lcm/lcops/api/v2/datacenters/{id}");
		log.debug("DAO Datacenter initialized");
	}
	
	@Override
	public Datacenter findById(Connection connection, String id) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", id);
		Datacenter dcObj = doApiRequest(connection, "GET", URL_GET_BY_VALUE, "{}", Datacenter.class, uriVariables);
		assignConnectionToObject(connection, dcObj);
		
		// Assign additional Properties (if possible - depends on service account access)
		try {
			List<Environment> dcEnvs = getEnvironments(connection, id);
			dcObj.setEnvironments(dcEnvs);
			List<VirtualCenter> dcVirtualCenters = getVirtualCenters(connection, dcObj);
			dcObj.setVirtualCenters(dcVirtualCenters);
		} catch (RuntimeException e) {
			log.error("Unable to add Environment/vCenter objects to the Datacenter, likely due to API access limitations for account ["+connection.getUserName()+"]");
		}
		return dcObj;
	}

	@Override
	public List<Datacenter> findAll(Connection connection) {
		List<Datacenter> allDc = new ArrayList<Datacenter>();
		Datacenter[] dcs = doApiRequest(connection, "GET", URL_GET_ALL, "{}", Datacenter[].class, null);
		
		if(dcs != null && dcs.length > 0) {
			for (Datacenter dc : dcs) {
				assignConnectionToObject(connection, dc);
				allDc.add(dc);
			}
		} else {
			log.debug("findAll() returned no datacenters or had an issue");
		}
		return allDc;
	}


	@Override
	public Datacenter create(Connection connection, Datacenter entity) {
		String body = null;
		try {
			body = vroObjectMapper.writeValueAsString(entity);
		} catch (JsonProcessingException e) {
			log.error("There was an error serializing the Datacenter entity into valid JSON!");
			e.printStackTrace();
		}
		Datacenter dc = doApiRequest(connection, "POST", URL_GET_ALL, body, Datacenter.class, null);
		assignConnectionToObject(connection, dc);
		return dc;
	}

	@Override
	public Datacenter update(Connection connection, Datacenter original, Datacenter replacement) {
		String body = null;
		try {
			body = vroObjectMapper.writeValueAsString(replacement);
		} catch (JsonProcessingException e) {
			log.error("There was an error parsing the JSON in the Datacenter update payload: "+e.getMessage());
			e.printStackTrace();
		}
		
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", original.getResourceId());
		
		return doApiRequest(connection, "PUT", URL_GET_BY_VALUE, body, Datacenter.class, uriVariables);
	}

	@Override
	public Request delete(Connection connection, Datacenter entity) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", entity.getResourceId());
		Request deleteRequest = doApiRequest(connection, "DELETE", URL_GET_BY_VALUE, null, Request.class, uriVariables);
		assignConnectionToObject(connection, deleteRequest);
		return deleteRequest;
	}

	/**
	 * Return a list of Environments associated with a particular Datacenter.
	 * @param nameOrId Name or ID of the Datacenter
	 * @return List of Environments
	 */
	public List<Environment> getEnvironments(Connection connection, String nameOrId) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", nameOrId);
		return Arrays.asList(doApiRequest(connection, "GET", URL_GET_ENV, null, Environment[].class, uriVariables) );
	}
	
	/**
	 * Return a list of vCenters associated with a particular Datacenter.
	 * @param nameOrId Name or ID of the Datacenter
	 * @return List of vCenter Servers
	 */
	public List<VirtualCenter> getVirtualCenters(Connection connection, Datacenter dc) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", dc.getResourceId());
		String vcRequest = doApiRequest(connection, "GET", URL_GET_VC, null, String.class, uriVariables);
		log.debug("getVirtualCenters() - "+vcRequest);
		VirtualCenter[] vcs = null;
		try {
			vcs = vroObjectMapper.readerFor(VirtualCenter[].class).readValue(vcRequest);
		} catch (JsonProcessingException e) {
			log.error("There was an error retrieving the vCenter servers in the JSON response: "+e.getMessage());
			e.printStackTrace();
		}
		assignConnectionToArray(connection, vcs);

		// Assign additional values
		for(VirtualCenter vc: vcs) {
			vc.setDatacenter(dc);
			vc.setLockerCredential(credentialService.getByValue(connection, new LockerReference(vc.getLockerReference()).getResourceId() ) );
		}
		return Arrays.asList(vcs);
	}
}
