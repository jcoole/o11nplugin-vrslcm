package com.sprockitconsulting.vrslcm.plugin.dao;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionAuthentication;
import com.sprockitconsulting.vrslcm.plugin.scriptable.BaseLifecycleManagerObject;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;

/**
 * This class is the basis for all Data Access Object functionality across all types.
 * Since *all* DAO related objects require a Connection object for initialization, there is only one constructor with that parameter.
 * 
 */

@Repository
public abstract class DaoAbstract<T> implements IDaoGeneric<T> {
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(DaoAbstract.class);
	
	protected String URL_GET_ALL;
	protected String URL_GET_BY_VALUE;
	
	@Autowired
	protected RestTemplate vroRestTemplate;
	@Autowired
	protected ObjectMapper vroObjectMapper;
	@Autowired
	protected ApplicationContext context;


	public DaoAbstract() {}
	
	/**
	 * Assigns the standard 'getAll' URL value for the type. Typically used on Get All and Create methods.
	 */
	public void setGetAllUrl(final String getAllUrl) {
		URL_GET_ALL = getAllUrl;
	}
	
	/**
	 * Assigns the standard 'getByID' URL value for the type. Typically used on Get, Update, Delete methods.
	 */
	public void setGetByValueUrl(final String getByValueUrl) {
		URL_GET_BY_VALUE = getByValueUrl;
	}
	
	/**
	 * This method is used after deserialization to assign the Connection and subsequently Internal ID to a single object.
	 * The Orchestrator Finder uses the 'internalId' value as its InventoryRef in the Factory for lookups.
	 * @param resource The resource to assign the connection to. This is generic, and the input must inherit from the BaseLifecycleManagerObject class.
	 * @see BaseLifecycleManagerObject
	 */
	@SuppressWarnings("hiding")
	protected <T extends BaseLifecycleManagerObject> void assignConnectionToObject(Connection connection, Object resource) {
		// First, extract the type and resourceId, used to generate the internalId.
		// Set the necessary object values. The internalId is used by the Finder accessor to do lookups in Plugin Factory.
		String internalId = ((BaseLifecycleManagerObject) resource).getResourceId()+"@"+connection.getId();
		((BaseLifecycleManagerObject) resource).setConnection(connection);
		((BaseLifecycleManagerObject) resource).setInternalId(internalId); 
	}
	
	/**
	 * This method is used after deserialization to assign the Connection ID to a list of resources.
	 * It simply calls the 'assignConnectionToObject' method for each object.
	 * @param resourceList The resource list object to assign the connection to. This is generic, and the objects in the list must inherit from the BaseLifecycleManagerObject class.
	 * @see BaseLifecycleManagerObject
	 */
	@SuppressWarnings("hiding")
	protected <T extends BaseLifecycleManagerObject> void assignConnectionToList(Connection connection, T[] resourceList) {
		for(Object resource: resourceList) {
			assignConnectionToObject(connection, resource);
		}
	}
	/**
	 * This is the core method that performs the exchange of requests and responses for objects in the LCM API.
	 * 
	 * @param <R> Generic that represents the Request Body for the API call, as this will differ based on the resource.
	 * @param <T> Generic that represents the Response Body for the API call as this will differ based on the resource.
	 * @param connection The Connection to use in the API call.
	 * @param method GET, POST, PUT, PATCH, DELETE
	 * @param urlTemplate The path to the endpoint that is being called, for example /myapi/v1/type/something.
	 * @param body The request body (JSON String) for the API call.
	 * @param responseType The class to map the response to, using the Jackson ObjectMapper -> POJO functionality.
	 * @param urlVariables This is a map of values to URI variables, such as /myapi/v1/type/{id}. If null, request will be made without it.
	 * @return The deserialized object of the specified responseType. 
	 * 		   If custom processing is needed, you can specify a String as return type and manipulate the returned value in the subclass method.
	 */
	@SuppressWarnings("hiding")
	protected <R,T> T doApiRequest(Connection connection, String method, String urlTemplate, R body, Class<T> responseType, Map<String, Object> urlVariables) {
		log.debug("doApiRequest("+method+", "+urlTemplate+") starting");

		// Define a generic response type value to hold the data before returning
		T response = null;
		
		// Setup the full URL for the call
		String baseUrl = "https://"+connection.getHost();
		String fullUrl = baseUrl+urlTemplate;
		
		// Convert the "Get" string to uppercase and look it up in the table.
		HttpMethod httpMethod = HttpMethod.resolve(method.toUpperCase());
		
		// Setup the HTTP Headers for the API request.
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Content-Type", "application/json");
		
		// Get the Authentication Token data.
		ConnectionAuthentication auth = connection.getConnectionAuthenticationFromRepository();
		// Validate the token isn't empty/missing/error state
		if(auth.isTokenValid()) {
			// proceed
			log.debug("doApiRequest : Connection Authentication seems valid, proceeding");
		} else {
			throw new RuntimeException("Authentication not valid: "+auth.toString() );
		}
		headers.set("Authorization", auth.getTokenType()+" "+auth.getToken());
		
		// Create the request entity using the body input and headers.
		HttpEntity<R> requestEntity = new HttpEntity<>(body, headers);

		// Using the Orchestrator Custom RestTemplate, execute the call and return based on 'responseType'
		try {
			ResponseEntity<T> responseEntity = null;
			
			// Check for URI Variables, omit if null.
			if(urlVariables != null) {
				log.debug("doApiRequest("+method+", "+fullUrl+", "+body+", URL Variables : "+urlVariables+" - performing exchange");
				responseEntity = vroRestTemplate.exchange(fullUrl, httpMethod, requestEntity, responseType, urlVariables);
			} else {
				log.debug("doApiRequest("+method+", "+fullUrl+", "+body+", No URL Variables - performing exchange");
				responseEntity = vroRestTemplate.exchange(fullUrl, httpMethod, requestEntity, responseType);
			}
			
			// Check the status code
			if(responseEntity.getStatusCodeValue() >= 200 && responseEntity.getStatusCodeValue() < 400) {
				response = responseEntity.getBody();
				log.debug("doApiRequest("+method+", "+urlTemplate+", "+body+") request completed, response : "+responseEntity.getStatusCode().getReasonPhrase());
			} else if(responseEntity.getStatusCodeValue() == 401) {
				// 401 is Unauthorized.
				log.error("ERROR! Unauthorized request to the API!");
			} else if(responseEntity.getStatusCodeValue() == 400) {
				// 400 is a bad request - typically means in this case that the user doesn't have rights to do it.
				// For this we log an error and allow it to continue.
				log.error("HTTP Status Code 400 (Bad Request) returned. Either the data submitted in this request isn't valid, or the user performing the request does not have rights to make it.");
			} else {
				log.error("Unhandled API Error - "+this.getClass().getEnclosingMethod().getName()+" error, status code ["+responseEntity.getStatusCodeValue()+"], body ["+responseEntity.getBody()+"]");
			}
		} catch (ResourceAccessException rae) {
			// This catches SSL exceptions and should be bubbled up to the user workflow.
			throw new ResourceAccessException("Resource Access Exception when making API call - "+rae.getMessage());
		} catch (HttpClientErrorException hce) {
			// This catches >400 status codes
			log.error("HttpClientErrorException found, status code ["+hce.getRawStatusCode()+"], response body :: "+hce.getResponseBodyAsString());
		} catch (RuntimeException e) {
			String rte = "RuntimeException when making API call ("+method+", "+urlTemplate+", "+body+") - "+e.getMessage();
			throw new RuntimeException(rte);
		}

		return response;
	}

}
