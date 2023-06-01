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
import com.sprockitconsulting.vrslcm.plugin.scriptable.Certificate;
import com.sprockitconsulting.vrslcm.plugin.scriptable.CertificateImport;
import com.sprockitconsulting.vrslcm.plugin.scriptable.CertificateInfo;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
/**
 * This class contains the data access and manipulation methods for the Certificate service.
 * @author justin
 *
 */
@Repository
public class DaoCertificate extends DaoAbstract<Certificate>
		implements IDaoGeneric<Certificate>, IDaoDelete<Certificate> {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(DaoCertificate.class);
	public DaoCertificate() {
		super();
		setGetAllUrl("/lcm/locker/api/v2/certificates");
		setGetByValueUrl("/lcm/locker/api/v2/certificates/{id}");
		log.debug("DAO Certificate initialized");
	}
	
	@Override
	public Certificate findById(Connection connection, String id) {
		// Check input
		if(connection == null) {
			throw new RuntimeException("You must specify a connection to perform the Certificate lookup!");
		}
		if(id.isBlank()) {
			throw new RuntimeException("You must specify a value to search for during Certificate lookup!");
		}
		
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", id);
		Certificate cert = doApiRequest(connection, "GET", URL_GET_BY_VALUE, "{}", Certificate.class, uriVariables);
		assignConnectionToObject(connection, cert);
		return cert;
	}

	@Override
	public List<Certificate> findAll(Connection connection) {
		// First, get the data back as a string
		String allCertsBody = doApiRequest(connection, "GET", URL_GET_ALL, "{}", String.class, null);
		
		// Read the body as a tree, and extract the certificates nested object.
		JsonNode allCertsObject = null;
		try {
			allCertsObject = vroObjectMapper.readTree(allCertsBody).path("certificates");
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
		assignConnectionToList(connection, certs);
		return Arrays.asList(certs);
	}

	@Override
	public Object delete(Connection connection, Certificate entity) {
		// Test for connection
		if(connection == null) {
			throw new RuntimeException("A Connection must be specified when deleting a Certificate!");
		}
		// Test for reference boolean.
		if(entity.isReferenced()) {
			throw new RuntimeException("The certificate is still in use by one or more products. Please try either removing the related product, or replacing the certificate with another one before trying this operation.");
		}
		Map<String, Object> uriVariables = new HashMap<>();
		uriVariables.put("id", entity.getResourceId());
		String req = doApiRequest(connection, "DELETE", URL_GET_BY_VALUE, "{}", String.class, uriVariables);
		return req;
	}

	/**
	 * Creates a Locker-signed SSL certificate with the given information.
	 * @param connection The LCM Server connection
	 * @param certificateInfo Information for the SSL certificate request.
	 * @return Certificate
	 */
	public Certificate createCertificate(Connection connection, CertificateInfo certificateInfo) {
		
		String body = null;
		try {
			body = vroObjectMapper.writeValueAsString(certificateInfo);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String response = doApiRequest(connection, "POST", URL_GET_ALL, body, String.class, null);
		log.debug("createCertificate response: "+response);
		
		// The HTTP response upon creating a certificate does not come back with a referenceable ID, so re-look it up by name/alias so it can be returned to the workflow.
		// There is probably a WAY more elegant way of doing this but...
		String[] aliases = new String[1];
		aliases[0] = certificateInfo.getNameOrAlias();
		List<Certificate> certs = getAllCertificatesMatchingAliases(connection, aliases);
		Certificate cert = certs.get(0);

		return cert;
	}
	
	/**
	 * Creates a Certificate Signing request with the given information.
	 * Once signed, user should invoke the importSignedCertificate() method.
	 * Initially since this comes back as an attachment, it's returned as a raw string.
	 * @param certificateInfo Information for the SSL certificate request.
	 * @return Base64 encoded CSR and Private Key
	 */
	public String createCertificateRequest(Connection connection, CertificateInfo certificateInfo) {
		String body = null;
		try {
			body = vroObjectMapper.writeValueAsString(certificateInfo);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String csr = doApiRequest(connection, "POST", URL_GET_ALL+"/csr", body, String.class, null);
		return csr;
	}
	
	/**
	 * Imports a signed SSL certificate, chain, and private key to the server.
	 */
	public Certificate importSignedCertificate(Connection connection, String name, String passphrase, String certificateChain, String privateKey) {
		CertificateImport importData = new CertificateImport(name, certificateChain, passphrase, privateKey);
		
		// Convert to JSON String for request
		String importDataBody = null;
		try {
			importDataBody = vroObjectMapper.writeValueAsString(importData);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Certificate cert = doApiRequest(connection, "POST", URL_GET_ALL+"/import", importDataBody, Certificate.class, null);
		assignConnectionToObject(connection, cert);
		return cert;
	}
	
	/**
	 * Get All Certificates matching one or more aliases
	 * @return Array of Certificates that match, if any
	 * @throws IOException 
	 */
	public List<Certificate> getAllCertificatesMatchingAliases(Connection connection, String[] aliases) {
		// Check input
		if(connection == null) {
			throw new RuntimeException("A Connection to check for certificates must be supplied!");
		}
		if(aliases == null || aliases.length < 1) {
			throw new RuntimeException("You must specify one or more aliases to search for!");
		}
		
		// Build the query string. A better URI builder should probably be used here but it works for now.
		String query = "?aliasQuery="+String.join("&",aliases);
		String searchUrl = URL_GET_ALL+query;
		
		// Return the response as a String and extract the array from '.certificates'
		String allCertsBody = doApiRequest(connection, "GET", searchUrl, "{}", String.class, null);
		JsonNode allCertsObject = null;
		try {
			allCertsObject = vroObjectMapper.readTree(allCertsBody).path("certificates");
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Certificate[] certs = null;
		try {
			certs = vroObjectMapper.readerFor(Certificate[].class).readValue(allCertsObject);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assignConnectionToList(connection, certs);
		return Arrays.asList(certs);
	}

}
