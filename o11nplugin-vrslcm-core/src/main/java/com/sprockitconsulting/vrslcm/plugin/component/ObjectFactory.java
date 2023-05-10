package com.sprockitconsulting.vrslcm.plugin.component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.sprockitconsulting.vrslcm.plugin.APIConstants;
import com.sprockitconsulting.vrslcm.plugin.scriptable.BaseLifecycleManagerObject;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Certificate;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.ConnectionInfo;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Credential;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Datacenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.sprockitconsulting.vrslcm.plugin.scriptable.VirtualCenter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper; 
import com.fasterxml.jackson.databind.ObjectWriter; 

/**
 * This is the core factory for object lookups and de/serialization in the plugin.
 * An instance of it is created for each Connection stored in the inventory so it can be referenced and re-used.
 * 
 * @author justin
 * @see ConnectionRepository
 */

@Component
@Qualifier(value = "objectFactory")
@Scope(value = "prototype")
public class ObjectFactory {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ObjectFactory.class);
	
	@Autowired // Lazy initialized
	private RestTemplate vroRestTemplate;
	@Autowired // Lazy initialized
	private ObjectMapper vroObjectMapper;
	
	private Connection connection;


	public ObjectFactory(Connection connection) {
		log.debug("Initializing ObjectFactory - for Connection ID ["+connection.getId()+"]");
		this.connection = connection;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public void setConnection(Connection connection) {
		this.connection = connection;
		log.debug("ObjectFactory connection value now set to "+connection.toString() );
	}
	
	/**
	 * This method is used after deserialization to assign the Connection ID and subsequently Internal ID to a single object
	 * @param resource The resource to assign the connection ID to. This is generic, and the input must inherit from the BaseLifecycleManagerObject class.
	 * @see BaseLifecycleManagerObject
	 */
	public <T extends BaseLifecycleManagerObject> void assignConnectionIdToObject(Object resource, String connectionId) {
		// First, extract the type and resourceId, used to generate the internalId.
		// Set the necessary object values. The internalId is used by the Finder accessor to do lookups in Plugin Factory.
		((BaseLifecycleManagerObject) resource).setConnectionId(connectionId);
		((BaseLifecycleManagerObject) resource).setInternalId(
				((BaseLifecycleManagerObject) resource).getResourceId()+"@"+connectionId);
	}
	
	/**
	 * This method is used after deserialization to assign the Connection ID to a list of resources.
	 * It simply calls the 'assignConnectionIdToObject' method for each object.
	 * @param resourceList The resource list object to assign the connection ID to. This is generic, and the objects in the list must inherit from the BaseLifecycleManagerObject class.
	 * @see BaseLifecycleManagerObject
	 */
	//public void assignConnectionIdToList(List<?> resourceList, String connectionId) {
	public <T extends BaseLifecycleManagerObject> void assignConnectionIdToList(T[] resourceList, String connectionId) {
		for(Object resource: resourceList) {
			assignConnectionIdToObject(resource, connectionId);
		}
	}

	// Datacenters
	public Datacenter getDatacentersByNameOrId(String nameOrId) {
		// Check input
		if(nameOrId.isBlank()) {
			throw new RuntimeException("You must specify a value to search for during Datacenter lookup!");
		}
		Datacenter dc = doApiRequest("GET", APIConstants.URI_LCOPS_DATACENTERS+nameOrId, "{}", Datacenter.class);
		assignConnectionIdToObject(dc, connection.getId());
		return dc;
	}

	public Datacenter[] getAllDatacenters() {
		Datacenter[] dcs = doApiRequest("GET", APIConstants.URI_LCOPS_DATACENTERS, "{}", Datacenter[].class);
		assignConnectionIdToList(dcs, connection.getId());
		return dcs;
	}
	
	public Datacenter createDatacenter(String name, String location) {
		Datacenter dcCreate = new Datacenter();
		dcCreate.setName(name);
		dcCreate.setLocation(location);
		
		// Use the injected ObjectMapper to convert the POJO to a JSON string.
		String dcPayload = null;
		log.debug("Begin createDatacenter("+name+", "+location+")");
		try {
			dcPayload = vroObjectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(dcCreate);
			log.debug("JSON payload : "+dcPayload);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return doApiRequest("POST", APIConstants.URI_LCOPS_DATACENTERS, dcPayload, Datacenter.class);
	}
	
	public Datacenter updateDatacenter(Datacenter dc, String name, String location) {
		Datacenter dcUpdate = new Datacenter();
		dcUpdate.setName(name);
		dcUpdate.setLocation(location);
		
		// Use the injected ObjectMapper to convert the POJO to a JSON string.
		String dcPayload = null;
		log.debug("Begin updateDatacenter("+dc.getResourceId()+", "+name+", "+location+")");
		try {
			dcPayload = vroObjectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(dcUpdate);
			log.debug("JSON payload : "+dcPayload);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return doApiRequest("PUT", APIConstants.URI_LCOPS_DATACENTERS, dcPayload, Datacenter.class);
	}
	
	public Request deleteDatacenter(Datacenter dc) {
		// requires checking for existing environments first.
		// you MUST look it up by ID to get values back!
		log.debug("Begin deleteDatacenter("+dc.getResourceId()+")");
		
		Environment[] existingEnvironments = null;
		try {
			log.debug("Checking for existing environments (if any) attached to Datacenter ["+dc.getResourceId()+"]");
			existingEnvironments = doApiRequest("GET", APIConstants.URI_LCOPS_DATACENTERS+dc.getResourceId()+"/environments", "{}", Environment[].class);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		
		
		// Check to see if the result is null or greater than 0. The array must be zero!
		// Return a request object if it goes through
		Request deleteRequest = null;
		if(existingEnvironments != null && existingEnvironments.length == 0) {
			log.debug("Datacenter ["+dc.getResourceId()+"] shows no registered environments, proceeding with DELETE operation.");
			try {
				deleteRequest = doApiRequest("DELETE", APIConstants.URI_LCOPS_DATACENTERS+dc.getResourceId(), null, Request.class);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		} else {
			throw new RuntimeException("Unable to delete the Datacenter ["+dc.name+"] with ID ["+dc.getResourceId()+"] - The Datacenter still has "+existingEnvironments.length+" environment associated to it, and they must be deleted first.");
		}
		
		return deleteRequest;
	}
	
	// VirtualCenters
	public VirtualCenter getVirtualCentersInDatacenterByNameOrId(String dcId, String nameOrId) {
		// Check input
		if(dcId.isBlank() || nameOrId.isBlank()) {
			throw new RuntimeException("Missing required value to search for during VirtualCenter lookup!");
		}
		String baseUrl = APIConstants.URI_LCOPS_VIRTUALCENTERS_BY_DC_AND_VALUE.replace("{dcId}",dcId).replace("{vc}",nameOrId);
		
		VirtualCenter vc = doApiRequest("GET", baseUrl, "{}", VirtualCenter.class);
		assignConnectionIdToObject(vc, connection.getId());
		return vc;
	}

	public VirtualCenter[] getAllVirtualCentersInDatacenter(String dcId) {
		String baseUrl = APIConstants.URI_LCOPS_VIRTUALCENTERS_BY_DC.replace("{dcId}", dcId);
		VirtualCenter[] vcs = doApiRequest("GET", baseUrl, "{}", VirtualCenter[].class);
		assignConnectionIdToList(vcs, connection.getId());
		return vcs;
		
	}
	
	// Environments
	public Environment getEnvironmentsByNameOrId(String nameOrId) {
		// Check input
		if(nameOrId.isBlank()) {
			throw new RuntimeException("You must specify a value to search for during Environment lookup!");
		}
		Environment env = doApiRequest("GET", APIConstants.URI_LCOPS_ENVIRONMENTS+nameOrId, "{}", Environment.class);
		assignConnectionIdToObject(env, connection.getId());
		return env;
	}

	public Environment[] getAllEnvironments() {
		Environment[] envs = doApiRequest("GET", APIConstants.URI_LCOPS_ENVIRONMENTS, "{}", Environment[].class);
		assignConnectionIdToList(envs, connection.getId());
		return envs;
	}
	
	// Requests
	public Request getRequestByNameOrId(String nameOrId) {
		// Check input
		if(nameOrId.isBlank()) {
			throw new RuntimeException("You must specify a value to search for during Request lookup!");
		}
		Request req = doApiRequest("GET", APIConstants.URI_LCOPS_ENVIRONMENTS+nameOrId, "{}", Request.class);
		assignConnectionIdToObject(req, connection.getId());
		return req;
	}
	
	// TODO: pagination or filtering options
	// TODO: convert to a builder pattern or create separate methods to filter out system/user requests?
	public Request[] getAllRequests() {
		Request[] allRequests = doApiRequest("GET", APIConstants.URI_LCOPS_REQUESTS, "{}", Request[].class);
		assignConnectionIdToList(allRequests, connection.getId());

		// Sorts the requests by the 'lastUpdatedOn' field, in ascending order.
		Collections.sort(Arrays.asList(allRequests), Comparator.comparing(Request::getLastUpdated)) ;
		
		// Reverses the list to show in descending order.
		Collections.reverse(Arrays.asList(allRequests));
		return allRequests;
	}
	
	/**
	 * Locker - Get All Certificates
	 * Of note - this is a paginated list, and the actual array of Certificates is in the nested object 'certificates'.
	 * So, some minor massaging of the data is required once returned from the API to convert it properly.
	 * @return Array of Certificates
	 * @throws IOException 
	 */
	public Certificate[] getAllCertificates() {
		// First, get the data back as a string
		String allCertsBody = doApiRequest("GET", APIConstants.URI_LOCKER_CERTIFICATES, "{}", String.class);
		// Read the body as a tree, and extract the certificates nested object.
		JsonNode allCertsObject = null;
		try {
			allCertsObject = vroObjectMapper.readTree(allCertsBody).path("certificates");
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Convert to Certificates array, and proceed
		Certificate[] certs = null;
		try {
			certs = vroObjectMapper.readerFor(Certificate[].class).readValue(allCertsObject);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assignConnectionIdToList(certs, connection.getId());
		return certs;
	}
	
	/**
	 * Locker - Get All Certificates matching aliases
	 * @return Array of Certificates that match, if any
	 */
	public Certificate[] getAllCertificatesMatchingAliases(String[] aliases) {
		// Check input
		if(aliases == null || aliases.length < 1) {
			throw new RuntimeException("You must specify one or more aliases to search for!");
		}
		// Build the query string. A better URI builder should probably be used here but it works for now.
		String query = "?aliasQuery="+String.join("&",aliases);
		String searchUrl = APIConstants.URI_LOCKER_CERTIFICATES+query;
		Certificate[] certs = doApiRequest("GET", searchUrl, "{}", Certificate[].class);
		assignConnectionIdToList(certs, connection.getId());
		return certs;
	}
	
	/**
	 * Locker - Get a Certificate by its ID value.
	 * @param id The ID to search on
	 * @return Certificate
	 */
	public Certificate getCertificateById(String aliasOrId) {
		// Check input
		if(aliasOrId.isBlank()) {
			throw new RuntimeException("You must specify a value to search for during Certificate lookup!");
		}
		Certificate cert = doApiRequest("GET", APIConstants.URI_LOCKER_CERTIFICATES+aliasOrId, "{}", Certificate.class);
		assignConnectionIdToObject(cert, connection.getId());
		return cert;
	}
	

	public Credential[] getAllCredentials() {
		// TODO Auto-generated method stub
		return null;
	}

	public Credential getCredentialByNameOrId(String nameOrId) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * This is the core method that performs the exchange of requests and responses for objects in the LCM API.
	 * 
	 * @param <R> Generic that represents the Request Body for the API call, as this will differ based on the resource.
	 * @param <T> Generic that represents the Response Body for the API call as this will differ based on the resource.
	 * @param method GET, POST, PUT, PATCH, DELETE
	 * @param urlTemplate The path to the endpoint that is being called. Check APIConstants for the list.
	 * @param body The request body for the API call.
	 * @param responseType The class to map the response to, using the Jackson ObjectMapper -> POJO functionality.
	 * @return The serialized object
	 */
	public <R, T> T doApiRequest(String method, String urlTemplate, R body, Class<T> responseType) {
		log.debug("doApiRequest("+method+", "+urlTemplate+", "+body+") starting, using connection ["+connection.toString()+"]");
		
		try {
			log.debug("vroRestTemplate : "+vroRestTemplate.toString()+", vroObjectMapper : "+vroObjectMapper.toString());
		} catch (RuntimeException e) {
			log.warn("Issue with template or mapper");
		}
		// Add the headers to the request entity
		// TODO: Move the token/header logic to separate method/class based on token type
		HttpHeaders headers = new HttpHeaders();
		String authToEncode = connection.getConnectionInfo().getUserName()+":"+connection.getConnectionInfo().getUserPassword();
		headers.set("Accept", "application/json");
		headers.set("Authorization", "Basic "+Base64.getEncoder().encodeToString((authToEncode).getBytes()) );
		
		// Define a generic response type value to hold the data before returning
		T response = null;
		
		// Convert the "Get" string to uppercase and look it up in the table.
		HttpMethod httpMethod = HttpMethod.resolve(method.toUpperCase());
		
		// Create the request entity using the body input and headers.
		HttpEntity<R> requestEntity = new HttpEntity<>(body, headers);
		
		// Setup the full URL for the call
		String fullUrl = "https://"+connection.getHost()+urlTemplate;
		
		// Using the Orchestrator Custom RestTemplate (auto-wired), execute the call and return based on 'responseType'
		log.debug("doApiRequest("+method+", "+fullUrl+", "+body+") performing exchange");
		try {
			ResponseEntity<T> responseEntity = vroRestTemplate.exchange(fullUrl, httpMethod, requestEntity, responseType);
			
			// Check the status code
			if(responseEntity.getStatusCodeValue() >= 200 && responseEntity.getStatusCodeValue() < 400) {
				response = responseEntity.getBody();
			
				log.debug("doApiRequest("+method+", "+urlTemplate+", "+body+") request completed, response : "+responseEntity.toString());
			} else {
				throw new RuntimeException(this.getClass().getEnclosingMethod().getName()+" error, status code ["+responseEntity.getStatusCodeValue()+"], body ["+responseEntity.getBody()+"]");
			}
		} catch (RuntimeException e) {
			log.warn("doApiRequest("+method+", "+urlTemplate+", "+body+") failed during exchange : "+e);
		}

		return response;
	}


	@Override
	public String toString() {
		return String.format("ObjectFactory [vroRestTemplate=%s, vroObjectMapper=%s, connection=%s]", vroRestTemplate,
				vroObjectMapper, connection);
	}

	
}
