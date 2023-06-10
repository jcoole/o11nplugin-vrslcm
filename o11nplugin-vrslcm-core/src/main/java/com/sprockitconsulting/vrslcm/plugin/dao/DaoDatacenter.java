package com.sprockitconsulting.vrslcm.plugin.dao;

import java.util.List;
import java.util.Map;

import java.io.IOException;
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
import com.sprockitconsulting.vrslcm.plugin.services.EnvironmentService;
import com.sprockitconsulting.vrslcm.plugin.services.VirtualCenterService;
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
	private String URL_GET_VC = URL_GET_BY_VALUE+"/vcenters";
	private String URL_GET_ENV = URL_GET_BY_VALUE+"/environments";
	
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
		Datacenter dcBean = (Datacenter) context.getBean("datacenter");
		Datacenter dcObj = doApiRequest(connection, "GET", URL_GET_BY_VALUE, "{}", Datacenter.class, uriVariables);
		try {
			vroObjectMapper.readerForUpdating(dcBean).readValue(vroObjectMapper.writeValueAsString(dcObj), Datacenter.class);
		} catch (IOException e) {
			log.error("There was an error updating the Datacenter bean during the findById method: "+e.getMessage());
			e.printStackTrace();
		}
		assignConnectionToObject(connection, dcBean);
		
		// Assign additional Properties (if possible - depends on service account access)
		try {
			dcBean.setEnvironments(getEnvironments(connection, id) );
			dcBean.setVirtualCenters(getVirtualCenters(connection, dcBean));
		} catch (RuntimeException e) {
			log.error("Unable to add Environment/vCenter objects to the Datacenter, likely due to API access limitations for account ["+connection.getUserName()+"]");
		}
		return dcBean;
	}

	@Override
	public List<Datacenter> findAll(Connection connection) {
		List<Datacenter> findings = new ArrayList<Datacenter>();
		Datacenter[] dcs = doApiRequest(connection, "GET", URL_GET_ALL, "{}", Datacenter[].class, null);
		
		if(dcs != null && dcs.length > 0) {
			for (Datacenter dc : dcs) {
				assignConnectionToObject(connection, dc);
				findings.add(dc);
			}
		} else {
			log.debug("findAll() returned no datacenters or had an issue");
		}
		return findings;
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
		// Create the Datacenter
		Datacenter dc = doApiRequest(connection, "POST", URL_GET_ALL, body, Datacenter.class, null);
		// After creation, perform a new lookup
		return findById(connection, dc.getResourceId());
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

	// TODO: Deletes turn into a requestID but sometimes not, so revisit.
	@Override
	public Object delete(Connection connection, Datacenter entity) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", entity.getResourceId());
		String deleteRequest = doApiRequest(connection, "DELETE", URL_GET_BY_VALUE, null, String.class, uriVariables);
		System.out.println(deleteRequest);
		Request deleteRequestObject = null;
		try {
			deleteRequestObject = vroObjectMapper.readerFor(Request.class).readValue(deleteRequest);
		} catch (JsonProcessingException e) {
			log.error("There was an error retrieving the Request for deleting the Datacenter ["+entity.getResourceId()+"] : "+e.getMessage());
			e.printStackTrace();
		}

		return deleteRequestObject;
	}

	/**
	 * Return a list of Environments associated with a particular Datacenter.
	 * @param nameOrId Name or ID of the Datacenter
	 * @return List of Environments
	 */
	public List<Environment> getEnvironments(Connection connection, String nameOrId) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", nameOrId);
		
		// Old version with additional parsing
		/*
		String environmentRequest = doApiRequest(connection, "GET", URL_GET_ENV, null, String.class, uriVariables);

		List<Environment> dcEnvs = null;
		try {
			dcEnvs = Arrays.asList(vroObjectMapper.readValue(environmentRequest, Environment[].class));
		} catch (JsonProcessingException e) {
			log.error("Unable to convert the Environment string response to object, error was : "+e.getMessage());
			e.printStackTrace();
		}
		*/
		
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
		assignConnectionToList(connection, vcs);
		log.debug("connections assigned to vc");
		// Assign additional values
		for(VirtualCenter vc: vcs) {
			log.debug("begin assign dc and cred");
			vc.setDatacenter(dc);
			log.debug("dc for "+vc.name+" set to "+vc.getDatacenter().toString());
			vc.setLockerCredential(credentialService.getByValue(connection, new LockerReference(vc.getLockerReference()).getResourceId() ) );
			log.debug("cred for "+vc.name+" set to "+vc.getLockerCredential().toString());
		}
		return Arrays.asList(vcs);
	}
}
