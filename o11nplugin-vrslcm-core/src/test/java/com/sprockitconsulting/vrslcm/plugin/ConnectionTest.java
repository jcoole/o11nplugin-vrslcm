package com.sprockitconsulting.vrslcm.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.ConnectionInfo;

@DisplayName("Connection-Specific Tests")
public class ConnectionTest {
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ConnectionTest.class);
	
	private final ConnectionInfo connectionInfo = new ConnectionInfo();
	private final ObjectMapper om = new ObjectMapper();
	private final String testFileLocation = "src/test/resources/test_connection.json";
	

	@Test
	void testInfoFileFoundAndParsed() {
		JsonNode json = null;
		try {
			json = om.readTree(new FileReader(testFileLocation));
			log.info("Parsed TestFile at ["+testFileLocation+"]");
		} catch (IOException e) {
			String err = "Couldn't parse the test connection file! Error was: "+e;
			log.error(err);
		}
		assertTrue(json != null);
	}
	@Test
	void infoIsCreated() {
		assertTrue(connectionInfo != null);
	}
	
	@Test
	void infoIsFullyCreated() {
		JsonNode json = null;
		try {
			json = om.readTree(new FileReader(testFileLocation));
		} catch (IOException e) {
			String err = "Couldn't parse the test connection file! Error was: "+e;
			log.error(err);
		}
		// Construct Info, check for ID
		ConnectionInfo info = new ConnectionInfo();
		info.setHost(json.get("vrslcmServer").asText());
		info.setName("TestConnection");
		info.setUserName(json.get("vrslcmUser").asText());
		info.setUserPassword(json.get("vrslcmPassword").asText());
		info.setUserDomain(json.get("vrslcmDomain").asText());
		
		assertTrue(info.getId() != null);
		log.info("Info has UUID "+info.getId());
	}

}
