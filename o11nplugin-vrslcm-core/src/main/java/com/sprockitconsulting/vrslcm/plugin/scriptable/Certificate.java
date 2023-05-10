package com.sprockitconsulting.vrslcm.plugin.scriptable;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
/**
 * Represents a Certificate object in LCM.
 * @author justin
 */
@JsonIgnoreProperties(ignoreUnknown = true) // if a field isn't mapped to a JSON property, skip it
@VsoObject(description = "Represents a Locker Certificate in LCM.")
@VsoFinder( //Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
	name = "Certificate", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "A LCM Certificate, either self-signed or signed by a trusted CA.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/certificate.png" // relative path to image in inventory use
)
public class Certificate extends BaseLifecycleManagerObject {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(Certificate.class);
	
	private String alias;
	private String algorithm;
	private String subject;
	private String issuer;
	private String sha1;
	private String sha256;
	private String keyLength;
	private String sans;
	private String expirationDate;
	private String issueDate;
	private String certChain;
	private String leafCert;
	private boolean isReferenced;
	private boolean isHealthy;
	
	public Certificate() {
		
	}
	
	// Getters

	@VsoProperty(description = "The Alias aka Friendly Name of the Certificate.")
	public String getAlias() {
		return alias;
	}
	@VsoProperty(description = "The encryption algorithm used in creating the Certificate.")
	public String getAlgorithm() {
		return algorithm;
	}
	@VsoProperty(description = "The Subject Name of the Certificate.")
	public String getSubject() {
		return subject;
	}
	@VsoProperty(description = "The Issuer of the Certificate.")
	public String getIssuer() {
		return issuer;
	}
	@VsoProperty(description = "The SHA-1 hash value of the Certificate.")
	public String getSha1() {
		return sha1;
	}
	@VsoProperty(description = "The SHA-256 hash value of the Certificate.")
	public String getSha256() {
		return sha256;
	}
	@VsoProperty(description = "The number of bits used to encrypt of the Certificate. Supported values are 2048/4096.")
	public String getKeyLength() {
		return keyLength;
	}
	@VsoProperty(description = "The Subject Alternative Name(s) in the Certificate.")
	public String getSans() {
		return sans;
	}
	@VsoProperty(description = "The date that the Certificate Expires.")
	public String getExpirationDate() {
		return expirationDate;
	}
	@VsoProperty(description = "The date that the Certificate was issued.")
	public String getIssueDate() {
		return issueDate;
	}
	@VsoProperty(description = "Indicates whether the Certificate is actively in use by a resource in LCM.")
	public boolean isReferenced() {
		return isReferenced;
	}
	@VsoProperty(description = "Certificate Health is whether or not its expired or close to it.")
	public boolean isHealthy() {
		return isHealthy;
	}
	@VsoProperty(description = "The Certificate Chain associated to the Certificate, base64 encoded.")
	public String getCertChain() {
		return certChain;
	}
	@VsoProperty(description = "The Certificate, base64 encoded.")
	public String getLeafCert() {
		return leafCert;
	}

	// Setters
	
	@JsonProperty("vmid")
	@Override
	public void setResourceId(String resourceId) {
		super.setResourceId(resourceId);
	}
	@JsonProperty("alias")
	public void setAlias(String alias) {
		this.alias = alias;
	}
	@JsonProperty("algorithm")
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	@JsonProperty("subject")
	public void setSubject(String subject) {
		this.subject = subject;
	}
	@JsonProperty("issuer")
	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}
	@JsonProperty("sha1")
	public void setSha1(String sha1) {
		this.sha1 = sha1;
	}
	@JsonProperty("referenced")
	public void setReferenced(boolean isReferenced) {
		this.isReferenced = isReferenced;
	}
	@JsonProperty("healthy")
	public void setHealthy(boolean isHealthy) {
		this.isHealthy = isHealthy;
	}
	@JsonProperty("certChain")
	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}
	@JsonProperty("leafCert")
	public void setLeafCert(String leafCert) {
		this.leafCert = leafCert;
	}
	
	// These values only appear when querying the certificate by ID.
	// See the helper methods below for their assignment during deserialization.
	
	public void setSha256(String sha256) {
		this.sha256 = sha256;
	}

	public void setKeyLength(String keyLength) {
		this.keyLength = keyLength;
	}

	public void setSans(String sans) {
		this.sans = sans;
	}

	public void setExpirationDate(String expirationDate) {
		this.expirationDate = expirationDate;
	}

	public void setIssueDate(String issueDate) {
		this.issueDate = issueDate;
	}
	
	// Helper Methods to parse the JSON nested objects.

	/**
	 * The 'validity' property is a child object in the JSON structure with some of the values we need.
	 */
	@JsonProperty("validity")
	private void getValidity(Map<String, Object> validity) {
		this.expirationDate = (String)validity.get("expiresOn");
		this.issueDate = (String)validity.get("issuedOn");
		this.isHealthy = (Boolean)validity.get("healthy");
	}
	/**
	 * The 'certInfo' property is a child object in the JSON structure with some of the values we need.
	 */
	@JsonProperty("certInfo") 
	private void getCertInfo(Map<String, Object> certInfo) {
		this.keyLength = (String)certInfo.get("keyLength");
		this.sans = (String)certInfo.get("san");
		this.sha256 = (String)certInfo.get("sha256");
	}
	
}
