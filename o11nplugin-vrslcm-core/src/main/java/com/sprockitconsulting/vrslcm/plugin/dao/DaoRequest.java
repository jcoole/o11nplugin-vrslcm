package com.sprockitconsulting.vrslcm.plugin.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;

@Repository
public class DaoRequest extends DaoAbstract<Request> {
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(DaoRequest.class);
	
	public DaoRequest() {
		super();
		setGetAllUrl("/lcm/request/api/v2/requests");
		setGetByValueUrl("/lcm/request/api/v2/requests/{id}");
		log.debug("DAO Request initialized");
	}
	
	@Override
	public Request findById(Connection connection, String id) {
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", id);
		Request req = doApiRequest(connection, "GET", URL_GET_BY_VALUE, "{}", Request.class, uriVariables);
		assignConnectionToObject(connection, req);
		return req;
	}

	@Override
	public List<Request> findAll(Connection connection) {
		Request[] reqs = doApiRequest(connection, "GET", URL_GET_ALL, "{}", Request[].class, null);
		
		// Assign Connection object
		assignConnectionToList(connection, reqs);
		
		// Convert java array to ArrayList for manipulation and eventual output
		List<Request> output = new ArrayList<Request>();
		output.addAll(Arrays.asList(reqs));
		
		// Sorts the requests by the 'lastUpdatedOn' field, in ascending order.
		Collections.sort(output, Comparator.comparing(Request::getLastUpdated)) ;
		
		// Reverses the list to show in descending order.
		Collections.reverse(output);
		
		// Go through the ArrayList and select only those of source type 'user' - system requests are usually generic and just clutter things.
		try {
			log.debug("trying user filtered request logic");
			log.debug("converted to list - total of ["+output.size()+"] entries in the list");
			output.removeIf(r -> r.getSource().equals("system") );
			log.debug("removed system requests - now ["+output.size()+"] entries in the list");
			log.debug("user requests :: "+output.toString());
			
		} catch (RuntimeException e) {
			log.debug("Couldn't extract user Requests from the array, error was: "+e);
		}
		return output;
	}
}
