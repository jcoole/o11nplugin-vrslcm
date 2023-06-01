package com.sprockitconsulting.vrslcm.plugin.utility;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionPrincipal;

/**
 * This class contains static methods that are helpful to others in a central location.
 * @author justin
 */
public class PluginUtility {

	public PluginUtility() {
		
	}
	
	/**
	 * Type agnostic Array.indexOf() method, since Java doesn't implement this natively in Arrays.
	 * @param <T>
	 * @param needle The thing to find
	 * @param haystack The stuff its in
	 */
	public static <T> int indexOf(T needle, T[] haystack) {
		// Assume it's not present by default
		int indexValue = -1;
		for (int i=0; i<haystack.length; i++) {
			if(haystack[i] != null && haystack[i].equals(needle) || needle == null && haystack[i] == null) {
				return i;
			}
		}
		return indexValue;
	}
	
	/**
	 * Authorization - This method returns the list of roles assigned to the account.
	 * Used in the Factory to handle who can do/see things in LCM.
	 * NOTE: Not used currently
	 * @throws IOException 
	 * @throws JsonProcessingException 
	 * @throws JsonMappingException 
	 */

/*
	public String[] getConnectionAuthorizedRoles() throws JsonMappingException, JsonProcessingException, IOException {
		String principalString = doApiRequest("GET", "/lcm/authzn/api/me", "{}", String.class);
		log.debug("whoami :: "+principalString);
		ConnectionPrincipal principal = vroObjectMapper.readerFor(ConnectionPrincipal.class).readValue(vroObjectMapper.readTree(principalString));
		return principal.getAuthorities();
	}
*/
}
