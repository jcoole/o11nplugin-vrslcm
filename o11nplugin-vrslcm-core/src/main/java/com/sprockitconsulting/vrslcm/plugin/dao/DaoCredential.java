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
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Credential;
/**
 * This class contains the data access and manipulation methods for the Credential service.
 * @author justin
 */
@Repository
public class DaoCredential extends DaoAbstract<Credential>
		implements IDaoCreate<Credential>, IDaoDelete<Credential>, IDaoGeneric<Credential>, IDaoUpdate<Credential> {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(DaoCredential.class);
	
	public DaoCredential() {
		super();
		setGetAllUrl("/lcm/locker/api/v2/passwords");
		setGetByValueUrl("/lcm/locker/api/v2/passwords/{id}");
		log.debug("DAO Credential initialized");
	}
	
	/**
	 * Get a Credential by its ID value.
	 * @param id The ID to search on
	 * @return Credential
	 */
	@Override
	public Credential findById(Connection connection, String id) {
		// Check input
		if(connection == null) {
			throw new RuntimeException("You must specify a Connection to use during lookups!");
		}
		if(id.isBlank()) {
			throw new RuntimeException("You must specify a value to search for during Credential lookup!");
		}
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", id);
		Credential cred = doApiRequest(connection, "GET", URL_GET_BY_VALUE, "{}", Credential.class, uriVariables);
		assignConnectionToObject(connection, cred);
		return cred;
	}
	
	/**
	 * Get All Credentials
	 * Of note - this is a paginated list, and the actual array of Credentials is in the nested object 'passwords'.
	 * So, some minor massaging of the data is required once returned from the API to convert it properly.
	 * @return Array of Credentials
	 */
	@Override
	public List<Credential> findAll(Connection connection) {
		// First, get the data back as a string
		String allCredsBody = doApiRequest(connection, "GET", URL_GET_ALL, "{}", String.class, null);
		
		// Read the body as a tree, and extract the 'passwords' nested object which contains the array of results.
		JsonNode allCredsObject = null;
		try {
			allCredsObject = vroObjectMapper.readTree(allCredsBody).path("passwords");
		} catch (JsonProcessingException e) {
			log.error("There was an error retrieving the Passwords in the JSON response: "+e.getMessage());
			e.printStackTrace();
		}

		// Convert to Credentials array, and proceed
		Credential[] creds = null;
		try {
			creds = vroObjectMapper.readerFor(Credential[].class).readValue(allCredsObject);
		} catch (IOException e) {
			log.error("There was an error converting the JSON into a List of Certificates: "+e.getMessage());
			e.printStackTrace();
		}
		assignConnectionToArray(connection, creds);
		return Arrays.asList(creds);
	}

	@Override
	public Credential update(Connection connection, Credential original, Credential replacement) {
		String updatedCredBody = null;
		try {
			updatedCredBody = vroObjectMapper.writeValueAsString(replacement);
		} catch (JsonProcessingException e) {
			log.error("There was an error parsing the update Credential payload: "+e.getMessage());
			e.printStackTrace();
		}
		
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", original.getResourceId());
		Credential cred = doApiRequest(connection, "PATCH", URL_GET_BY_VALUE, updatedCredBody, Credential.class, uriVariables);
		assignConnectionToObject(connection, cred);
		return cred;
	}

	@Override
	public Object delete(Connection connection, Credential entity) {
		// Test for reference boolean.
		if(entity.isReferenced()) {
			throw new RuntimeException("The credential is still in use by one or more products. Please try either removing the related product, or replacing the certificate with another one before trying this operation.");
		}
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", entity.getResourceId());
		doApiRequest(connection, "DELETE", URL_GET_BY_VALUE, "{}", String.class, uriVariables);
		return null;
	}

	@Override
	public Credential create(Connection connection, Credential entity) {
		String newCredBody = null;
		try {
			newCredBody = vroObjectMapper.writeValueAsString(entity);
		} catch (JsonProcessingException e) {
			log.error("There was an error parsing the JSON for Credential creation: "+e.getMessage());
			e.printStackTrace();
		}
		
		Credential cred = doApiRequest(connection, "POST", URL_GET_ALL, newCredBody, Credential.class, null);
		assignConnectionToObject(connection, cred);
		return cred;
	}

}
