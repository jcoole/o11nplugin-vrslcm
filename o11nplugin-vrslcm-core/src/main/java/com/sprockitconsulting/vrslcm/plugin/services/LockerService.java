package com.sprockitconsulting.vrslcm.plugin.services;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Certificate;
import com.sprockitconsulting.vrslcm.plugin.scriptable.CertificateInfo;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Credential;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;
/**
 * This class is a front-end used by the Connection object in Orchestrator as a means to communicate with the core ObjectFactory.
 * When initialized by the Connection class, the Connection ID is used to to also get the ObjectFactory instance.
 * 
 * This service handles Locker specific content.
 * @author justin
 */
@VsoObject(description = "Enables access to Locker specific methods for the server connection.")
public class LockerService extends AbstractService {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(LockerService.class);
	
	public LockerService() {
		super();
	}
	
	@VsoMethod(description = "Creates a new Locker-signed certificate based on inputs.")
	public Certificate createCertificate(@VsoParam(description = "The specifications of the certificate to request.")CertificateInfo info) throws JsonProcessingException {
		return objectFactory.createCertificate(info);
	}
	@VsoMethod(description = "Creates a Certificate Signing Request based on inputs. Once you have signed the certificate, you can import it using the [importCertificate] method.")
	public String createCertificateSigningRequest(@VsoParam(description = "The specifications of the certificate to request.")CertificateInfo info) throws JsonProcessingException {
		byte[] csr =  objectFactory.createCertificateRequest(info);
		log.debug("CSR as bytes: "+csr);
		return new String(csr, StandardCharsets.UTF_8);
	}
		
	@VsoMethod(description = "Retrieves all Certificates for this server.")
	public List<Certificate> getAllCertificates() {
		return Arrays.asList(objectFactory.getAllCertificates());
	}
	
	@VsoMethod(description = "Retrieves all Certificates for this server matching the list of aliases.")
	public List<Certificate> getAllCertificatesMatchingAliases(@VsoParam(description = "One more values to query for in the list. All matches will be returned.")String[] aliases) {
		return Arrays.asList(objectFactory.getAllCertificatesMatchingAliases(aliases));
	}
	
	@VsoMethod(description = "Retrieves a Certificate via ID on this server.")
	public Certificate getCertificateById(@VsoParam(description = "The ID to search for - also known in LCM as the 'vmid' value")String id) {
		return objectFactory.getCertificateById(id);
	}

	@VsoMethod(description = "Deletes a Certificate from the server. Must not be referenced by other managed entities.")
	public String deleteCertificate(@VsoParam(description = "The Certificate object to delete")Certificate cert) {
		return objectFactory.deleteCertificate(cert);
	}
	
	@VsoMethod(description = "Retrieves all Credentials for this server.")
	public List<Credential> getAllCredentials() {
		return Arrays.asList(objectFactory.getAllCredentials());
	}
	
	@VsoMethod(description = "Retrieves a Credential by name or resource ID on this server.")
	public Credential getCredentialByValue(@VsoParam(description = "The Name or ID to search in the credential list.")String nameOrId) {
		return objectFactory.getCredentialByNameOrId(nameOrId);
	}

}
