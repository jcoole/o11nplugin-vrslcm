package com.sprockitconsulting.vrslcm.plugin.dao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sprockitconsulting.vrslcm.plugin.products.AbstractProduct;
import com.sprockitconsulting.vrslcm.plugin.products.ProductNode;
import com.sprockitconsulting.vrslcm.plugin.products.ProductSnapshot;
import com.sprockitconsulting.vrslcm.plugin.products.ProductSnapshotRequest;
import com.sprockitconsulting.vrslcm.plugin.products.ProductSnapshotRequest.Create;
import com.sprockitconsulting.vrslcm.plugin.products.ProductSnapshotRequest.Delete;
import com.sprockitconsulting.vrslcm.plugin.products.ProductUpdateCredentialRequest;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Credential;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.sprockitconsulting.vrslcm.plugin.services.EnvironmentService;
/**
 * This class contains the data access and manipulation methods for the Environment Service and related content, such as Products and their respective Day 2 Actions.
 * @author justin
 */
@Repository
public class DaoEnvironment extends DaoAbstract<Environment> {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(DaoEnvironment.class);
	
	@Autowired
	private EnvironmentService environmentService;
	
	// Base URL for snapshot management.
	private String snapshotInventoryUrl = "/lcm/lcops/api/environments/{environmentId}/products/{productId}/snapshot/inventory";

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
		//env.assignFinderIdValuesToProductsAndNodes();
		assignFinderIdValuesToProductsAndNodes(env);
		
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
	
	/**
	 * Environment sync request.
	 * @param connection The LCM Server Connection
	 * @param id Environment ID
	 * @return Environment sync requests - one for each product in the environment
	 */
	public List<Request> environmentSyncRequest(Connection connection, String id) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", id);
		Request[] requests = doApiRequest(connection, "POST", URL_GET_BY_VALUE+"/inventory-sync", "{}", Request[].class, uriVariables);
		assignConnectionToArray(connection, requests);
		return Arrays.asList(requests);
	}
	
	public Request productSyncRequest(Connection connection, String environmentId, String productId) {
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
	
	// Snapshot requests.
	public Request createSnapshotRequest(Connection connection, String environmentId, String productId, String snapshotDescription, String snapshotPrefix, Boolean snapshotMemory, Boolean snapshotShutdown) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("environmentId", environmentId);
		uriVariables.put("productId", productId);
		String url = "/lcm/lcops/api/environments/{environmentId}/products/{productId}/snapshot/inventory";
		
		// Create a snapshot request as object, which will be turned to a JSON string.
		ProductSnapshotRequest.Create createRequest = new Create(snapshotDescription, snapshotPrefix, snapshotShutdown, snapshotMemory);
		String snapshotBody = null;
		try {
			snapshotBody = vroObjectMapper.writeValueAsString(createRequest);
		} catch (JsonProcessingException e) {
			log.error("Unable to create body for the snapshot creation request: "+e.getMessage());
			e.printStackTrace();
		}
		Request req = doApiRequest(connection, "POST", url, snapshotBody, Request.class, uriVariables);
		assignConnectionToObject(connection, req);
		return req;
	}
	public List<ProductSnapshot> findProductSnapshots(Connection connection, String environmentId, String productId) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("environmentId", environmentId);
		uriVariables.put("productId", productId);
		
		List<ProductSnapshot> req = Arrays.asList(doApiRequest(connection, "GET", snapshotInventoryUrl, "{}", ProductSnapshot[].class, uriVariables));
		assignConnectionToList(connection, req);
		
		// Assign the environment/product to the return values.
		req.forEach(snap -> snap.setEnvironmentId(environmentId));
		req.forEach(snap -> snap.setProductId(productId));

		return req;
	}

	public Request deleteSnapshotRequest(Connection connection, String environmentId, String productId,	ProductSnapshot snapshot) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("environmentId", environmentId);
		uriVariables.put("productId", productId);
		
		// Create a snapshot request as object, which will be turned to a JSON string.
		ProductSnapshotRequest.Delete deleteRequest = new Delete(snapshot.getResourceId());
		String snapshotBody = null;
		try {
			snapshotBody = vroObjectMapper.writeValueAsString(deleteRequest);
		} catch (JsonProcessingException e) {
			log.error("Unable to create body for the snapshot delete request: "+e.getMessage());
			e.printStackTrace();
		}

		Request req = doApiRequest(connection, "DELETE", snapshotInventoryUrl, snapshotBody, Request.class, uriVariables);
		assignConnectionToObject(connection, req);
		return req;
	}
	
	// Helper and miscellaneous methods
	
	/**
	 * Used to update Product and Node internalId values once the Connection is assigned.
	 * Since Products and their nodes are directly attached to Environments and not independently manageable, it's not possible to inject services into them otherwise.
	 * Thus, the environmentService spring bean is assigned using this helper method when the Environment is deserialized.
	 * @param environment The environment to update prior to returning to the user.
	 */
	public void assignFinderIdValuesToProductsAndNodes(Environment environment) {

		for (AbstractProduct product : environment.getProducts()) {

			// Products are unique per Environment/Connection. The 'productId' string used in lookup is handled in the Finder methods.
			String productInternalId = environment.getResourceId()+"@"+environment.getConnection().getId();
			product.setInternalId(productInternalId);
			product.setConnection(environment.getConnection());
			product.setEnvironmentId(environment.getResourceId());
			product.setEnvironmentService(environmentService);
			
			log.debug("Assigned Internal ID, Connection, Environment ID values to product ["+productInternalId+"]");
			
			// Nodes internalId follows this format -- [vmName]:[type]:[product]:[environmentId]@[connectionId]
			for (ProductNode node : product.getProductNodes()) {
				String vmName = node.getProductNodeSpec().getNodeProperty("vmName").toString();
				String nodeInternalId = vmName+":"+node.getType()+":"+product.getProductId()+":"+environment.getResourceId();
				node.setName(vmName+" - "+node.getType());
				node.setInternalId(nodeInternalId);
				node.setConnection(environment.getConnection());
				node.setEnvironmentService(environmentService);
				log.debug("Assigned internal ID to node ["+nodeInternalId+"]");
			}
		}
	}
}
