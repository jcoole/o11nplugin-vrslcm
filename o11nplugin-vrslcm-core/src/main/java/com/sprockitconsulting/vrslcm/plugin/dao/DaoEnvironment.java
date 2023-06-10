package com.sprockitconsulting.vrslcm.plugin.dao;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
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
		assignConnectionToList(connection, envs);
		return Arrays.asList(envs);
	}

}
