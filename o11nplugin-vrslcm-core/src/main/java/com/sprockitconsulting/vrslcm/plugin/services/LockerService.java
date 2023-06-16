package com.sprockitconsulting.vrslcm.plugin.services;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Certificate;
import com.sprockitconsulting.vrslcm.plugin.scriptable.CertificateInfo;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
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
@Component
@Scope(value = "prototype")
@VsoObject(description = "Enables access to Locker specific methods for the server connection.")
public class LockerService extends AbstractService {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(LockerService.class);
	
	@Autowired
	private CertificateService certificateService;
	@Autowired
	private CredentialService credentialService;
	
	public LockerService(Connection connection) {
		super(connection);
		log.debug("Locker Service with Connection ["+connection.getId()+"] initialized");
	}
	
	// Certificates
	@VsoMethod(description = "Creates a new Locker-signed certificate based on inputs.")
	public Certificate createCertificate(@VsoParam(description = "The specifications of the certificate to request.")CertificateInfo info) {
		Certificate cert = null;
		cert = certificateService.createCertificate(connection, info);
		return cert;
	}
	
	@VsoMethod(description = "Creates a Certificate Signing Request based on inputs. Once you have signed the certificate, you can import it using the @importCertificate method.")
	public String createCertificateSigningRequest(@VsoParam(description = "The specifications of the certificate to request.")CertificateInfo info) throws JsonProcessingException {
		String csr = certificateService.createCertificateSigningRequest(connection, info);
		return csr;
	}
		
	@VsoMethod(description = "Retrieves all Certificates for this server.")
	public List<Certificate> getAllCertificates() {
		return certificateService.getAllCertificates(connection);
	}
	
	@VsoMethod(description = "Retrieves all Certificates for this server matching the list of aliases.")
	public List<Certificate> getAllCertificatesMatchingAliases(@VsoParam(description = "One more values to query for in the list. All matches will be returned.")String[] aliases) throws IOException {
		return certificateService.getCertificatesByAliases(connection, aliases);
	}
	
	@VsoMethod(description = "Retrieves a Certificate via ID on this server.")
	public Certificate getCertificateById(@VsoParam(description = "The ID to search for - also known in LCM as the 'vmid' value")String id) {
		return certificateService.getCertificateByValue(connection, id);
		
	}

	@VsoMethod(description = "Deletes a Certificate from the server. Must not be referenced by other managed entities.")
	public void deleteCertificate(@VsoParam(description = "The Certificate object to delete")Certificate cert) {
		certificateService.deleteCertificate(connection, cert);
	}
	
	@VsoMethod(description = "Imports a signed Certificate chain and private key to the server.")
	public Certificate importTrustedCertificate(
		@VsoParam(description = "Display name of the Certificate being imported.") String name,
		@VsoParam(description = "The full certificate chain as a Base64-encoded string. The order from top to bottom should be the Leaf certificate, Intermediate CA(s), then Root CA") String certificateChain,
		@VsoParam(description = "Private key") String privateKey,
		@VsoParam(description = "Passphrase used to decrypt the private key if used") String passphrase
		) {
		return certificateService.importCertificate(connection, name, passphrase, certificateChain, privateKey);
	}
	
	
	// Credentials
	@VsoMethod(description = "Retrieves all Credentials for this server.")
	public List<Credential> getAllCredentials() {
		return credentialService.getAll(connection);
	}
	
	@VsoMethod(description = "Retrieves a Credential by name or resource ID on this server.")
	public Credential getCredentialByValue(@VsoParam(description = "The Name or ID to search in the credential list.")String nameOrId) {
		return credentialService.getByValue(connection, nameOrId);
	}
	
	@VsoMethod(description = "Creates a new Credential with the specified values.")
	public Credential createCredential(
			@VsoParam(description = "The Alias/Name to assign to the credential.")String alias, 
			@VsoParam(description = "The Description to assign to the credential.")String description, 
			@VsoParam(description = "The Password to specify for this credential.")String password, 
			@VsoParam(description = "The associated username for this credential, for example root or sshuser.")String userName
	) {
		return credentialService.create(connection, new Credential(alias, userName, password, description));
	}
	@VsoMethod(description = "Updates an existing Credential with the specified values.")
	public Credential updateCredential(
			@VsoParam(description = "The credential to update.")Credential credential,
			@VsoParam(description = "The Alias/Name to assign to the credential.")String alias, 
			@VsoParam(description = "The Description to assign to the credential.")String description, 
			@VsoParam(description = "The Password to specify for this credential.")String password, 
			@VsoParam(description = "The associated username for this credential, for example root or sshuser.")String userName
	) {
		return credentialService.update(connection, credential, new Credential(alias, userName, password, description));

	}
	
	@VsoMethod(description = "Deletes an existing Credential. If the credential is in use, this will fail!")
	public void deleteCredential(
			@VsoParam(description = "The credential to delete.")Credential credential
	) {
		credentialService.delete(connection, credential);
	}
/*
 * TODO: Maybe delete this
	public <T extends BaseLifecycleManagerObject> T getLockerResourceByReference(String reference) {
		T resource = null;
		switch(type) {
		case "certificate":
			resource = (T) certificateService.getCertificateByValue(connection, resourceId);
			break;
		case "password":
			resource = (T) credentialService.getByValue(connection, resourceId);
			break;
		}
		return resource;
	}
*/
	@Override
	public String toString() {
		return String.format("LockerService [connection=%s]", connection);
	}
	
	
}
