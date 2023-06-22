package com.sprockitconsulting.vrslcm.plugin.dao;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sprockitconsulting.vrslcm.plugin.products.ProductSnapshotRequest;
import com.sprockitconsulting.vrslcm.plugin.products.ProductUpdateCredentialRequest;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Credential;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
/**
 * This class contains the data access and manipulation methods for the Environment Service.
 * @author justin
 */
@Repository
public class DaoEnvironment extends DaoAbstract<Environment> {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(DaoEnvironment.class);

	public DaoEnvironment() {
		super();
		setGetAllUrl("/lcm/lcops/api/v2/environments");
		setGetByValueUrl("/lcm/lcops/api/v2/environments/{id}");
		log.debug("DAO Environment initialized");
	}
	
	@Override
	public Environment findById(Connection connection, String id) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", id);
		String environmentString = doApiRequest(connection, "GET", URL_GET_BY_VALUE, "{}", String.class, uriVariables);
		
		// Parse the API response. We want to get the 'environmentData' object which contains what we want.
		JsonNode rootObject = null;
		try {
			rootObject = vroObjectMapper.readTree(environmentString);
		} catch (IOException e) {
			throw new RuntimeException("Unable to parse the API JSON response into a valid JSON Node! Exception was: "+e);
		}
		
		// Use the application context to generate a prototyped bean of the Environment.
		Environment env = (Environment) context.getBean("environment");
		
		// Taking the JSON Node with the response, extract the 'environmentData' object as text.
		// Inline re-parse the environmentData into a JSON Node and pass it to the reader and update the bean.
		// Doing this causes the object to be fully deserialized and contains the infrastructure properties mapped.
		try {
			vroObjectMapper.readerForUpdating(env).readValue(vroObjectMapper.readTree(rootObject.path("environmentData").asText() ) );
		} catch (IOException e) {
			throw new RuntimeException("Unable to update the Environment bean with the JSON data! Exception was: "+e);
		}
		
		// Assign additional properties prior to return.
		assignConnectionToObject(connection, env);
		
		// Update Product and Product Node IDs
		env.assignFinderIdValuesToProductsAndNodes();
		
		return env;
	}

	@Override
	public List<Environment> findAll(Connection connection) {
		Environment[] envs = doApiRequest(connection, "GET", URL_GET_ALL, "{}", Environment[].class, null);
		assignConnectionToArray(connection, envs);
		return Arrays.asList(envs);
	}

	// Day 2 Actions for Products in the Environment.
	// Some products support them and some don't. The service layer handles that logic.
	public Request powerOffRequest(Connection connection, String environmentId, String productId) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", environmentId);
		uriVariables.put("productId", productId);
		String url = URL_GET_BY_VALUE+"/products/{productId}/power-off";
		
		Request req = doApiRequest(connection, "POST", url, "{}", Request.class, uriVariables);
		assignConnectionToObject(connection, req);
		return req;
	}
	
	public Request powerOnRequest(Connection connection, String environmentId, String productId) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", environmentId);
		uriVariables.put("productId", productId);
		String url = URL_GET_BY_VALUE+"/products/{productId}/power-on";
		
		Request req = doApiRequest(connection, "POST", url, "{}", Request.class, uriVariables);
		assignConnectionToObject(connection, req);
		return req;
	}
	
	public Request createSnapshotRequest(Connection connection, String environmentId, String productId, String snapshotDescription, String snapshotPrefix, Boolean snapshotMemory, Boolean snapshotShutdown) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", environmentId);
		uriVariables.put("productId", productId);
		String url = URL_GET_BY_VALUE+"/products/{productId}/snapshot/inventory";
		
		ProductSnapshotRequest snapshotRequest = new ProductSnapshotRequest(snapshotDescription, snapshotPrefix, snapshotShutdown, snapshotMemory);
		String snapshotBody = null;
		try {
			snapshotBody = vroObjectMapper.writeValueAsString(snapshotRequest);
		} catch (JsonProcessingException e) {
			log.error("Unable to create body for the snapshot creation request: "+e.getMessage());
			e.printStackTrace();
		}
		Request req = doApiRequest(connection, "POST", url, snapshotBody, Request.class, uriVariables);
		assignConnectionToObject(connection, req);
		return req;
	}
	
	public Request inventorySyncRequest(Connection connection, String environmentId, String productId) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", environmentId);
		uriVariables.put("productId", productId);
		String url = URL_GET_BY_VALUE+"/products/{productId}/inventory-sync";
		
		Request req = doApiRequest(connection, "POST", url, "{}", Request.class, uriVariables);
		assignConnectionToObject(connection, req);
		return req;
	}
	
	public Request performHealthCheckRequest(Connection connection, String environmentId, String productId) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", environmentId);
		uriVariables.put("productId", productId);
		String url = URL_GET_BY_VALUE+"/products/{productId}/health-check";
		
		Request req = doApiRequest(connection, "POST", url, "{}", Request.class, uriVariables);
		assignConnectionToObject(connection, req);
		return req;
	}
	
	public Request updateAdminCredentialRequest(Connection connection, String environmentId, String productId, Credential currentCredential, Credential updatedCredential) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", environmentId);
		uriVariables.put("productId", productId);
		String url = URL_GET_BY_VALUE+"/products/{productId}/admin-password";
		
		ProductUpdateCredentialRequest updateRequest = new ProductUpdateCredentialRequest(currentCredential, updatedCredential);
		String updateBody = null;
		try {
			updateBody = vroObjectMapper.writeValueAsString(updateRequest);
		} catch (JsonProcessingException e) {
			log.error("Unable to create JSON body for update admin password request: "+e.getMessage());
			e.printStackTrace();
		}
		
		Request req = doApiRequest(connection, "PUT", url, updateBody, Request.class, uriVariables);
		assignConnectionToObject(connection, req);
		return req;
	}
}
