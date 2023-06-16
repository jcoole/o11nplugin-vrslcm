package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.regex.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoConstructor;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * This class is a helper for Certificate requests, either for self-signed or CSRs.
 * Contains all validation for inputs prior to submit.
 * From the user perspective this behaves just like a 'ConnectionInfo' in that it must be constructed first before being passed to the specific methods.
 * @author justin
 */
@VsoObject(description = "Container for a SSL Certificate whether it is self-signed by Locker, or used to generate a signing request for external CA.")
public class CertificateInfo {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ConnectionInfo.class);
	
	private String nameOrAlias;
	private String commonName;
	private String org;
	private String orgUnit;
	private String country;
	private String locality;
	private String state;
	private int keyLength = 2048;
	private String[] sans;
	private int validity = 1095;
	private String tenant = "default";
	
	@VsoConstructor(description = "Empty Default Constructor. Assign the required values in your scripts.")
	public CertificateInfo() {}
	
	@VsoConstructor(description = "Default Constructor with all required fields.")
	public CertificateInfo(
			@VsoParam(description = "Name/Alias of the Certificate.", required = true)String nameOrAlias, 
			@VsoParam(description = "Common Name (CN) used on the Certificate.", required = true)String commonName, 
			@VsoParam(description = "Organization field used on the Certificate.", required = true)String org, 
			@VsoParam(description = "Organizational Unit field used on the Certificate.", required = true)String orgUnit, 
			@VsoParam(description = "Country (US, JP, CN) used on the Certificate.", required = true)String country, 
			@VsoParam(description = "Locality/City field used on the Certificate.", required = true)String locality, 
			@VsoParam(description = "State field used on the Certificate.", required = true)String state, 
			@VsoParam(description = "Encryption key size used on the Certificate, default is 2048.")int keyLength, 
			@VsoParam(description = "List of Subject Alternative Names for the Certificate. These need to be Fully-Qualified Domain Name entries.")String[] sans,
			@VsoParam(description = "Tenant Name")String tenant) {
		this.commonName = commonName;
		this.country = country;
		this.nameOrAlias = nameOrAlias;
		this.org = org;
		this.orgUnit = orgUnit;
		this.locality = locality;
		this.state = state;
		this.keyLength = keyLength;
		this.sans = sans;
		this.tenant = tenant;
	}

	@VsoProperty(description = "Name/Alias of the Certificate.", required = true)
	public String getNameOrAlias() {
		return nameOrAlias;
	}
	
	@VsoProperty(description = "Common Name (CN) used on the Certificate.", required = true)
	public String getCommonName() {
		return commonName;
	}

	@VsoProperty(description = "Organization field used on the Certificate.", required = true)
	public String getOrg() {
		return org;
	}
	
	@VsoProperty(description = "Organizational Unit field used on the Certificate.", required = true)
	public String getOrgUnit() {
		return orgUnit;
	}
	
	@VsoProperty(description = "Country (US, JP, CN) used on the Certificate.", required = true)
	public String getCountry() {
		return country;
	}
	
	@VsoProperty(description = "Locality/City field used on the Certificate.", required = true)
	public String getLocality() {
		return locality;
	}
	
	@VsoProperty(description = "State field used on the Certificate.", required = true)
	public String getState() {
		return state;
	}
	
	@VsoProperty(description = "Encryption key size used on the Certificate, default is 2048.")
	public int getKeyLength() {
		return keyLength;
	}
	
	@VsoProperty(description = "List of Subject Alternative Names for the Certificate. These need to be Fully-Qualified Domain Name entries.")
	public String[] getSans() {
		return sans;
	}
    @JsonProperty("validity")
	public int getValidity() {
		return validity;
	}
    @VsoProperty(description = "Tenant Name")
    @JsonProperty("tenant")
	public String getTenant() {
		return tenant;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	
	@JsonProperty("alias")
	public void setNameOrAlias(String nameOrAlias) {
		this.nameOrAlias = nameOrAlias;
	}
	
	@JsonProperty("cN")
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	
	@JsonProperty("o")
	public void setOrg(String org) {
		this.org = org;
	}

	@JsonProperty("oU")
	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}

	@JsonProperty("c")
	public void setCountry(String country) {
		this.country = country;
	}

	@JsonProperty("l")
	public void setLocality(String locality) {
		this.locality = locality;
	}

	@JsonProperty("sT")
	public void setState(String state) {
		this.state = state;
	}

	@JsonProperty("size")
	public void setKeyLength(int keyLength) {
		if(keyLength != 2048 || keyLength != 4096) {
			keyLength = 2048;
		}
		this.keyLength = keyLength;
	}

	@JsonProperty("host")
	public void setSans(String[] sans) {
		this.sans = sans;
	}

	/**
	 * Validates the Certificate Info values.
	 */
	@VsoMethod(description = "Validates inputs prior to usage.")
	public boolean validateInfo() {
		boolean isValid = true;
		// Check if CommonName is FQDN
		if (!isFqdn(this.commonName) ) {
			throw new IllegalArgumentException("The common name must be a valid FQDN. Please try again with a different value.");
		}
		// Check if CN is in the SANS list. LCM requires this.
		if (indexOfArray(sans, commonName) == -1) {
			log.debug("validateInfo() - adding missing CN to the SAN list.");
			String[] updatedSans = Arrays.copyOf(sans, sans.length+1);
			updatedSans[sans.length] = commonName;
			this.sans = updatedSans;
		}
		return isValid;
	}
	
	/**
	 * Validates if the value is a FQDN.
	 */
	private boolean isFqdn(String str) {
		return Pattern.compile("(?=^.{4,253}$)(^((?!-)[a-zA-Z0-9-]{1,63}(?<!-)\\.)+[a-zA-Z]{2,63}$)").matcher(str).matches();
	}
	
	/** 
	 * Searches an array for a value. Java arrays of primitives do not have 'indexOf()' so this is a substitute.
	 */
	private static int indexOfArray(String[] array, String key) {
	    int returnvalue = -1;
	    for (int i = 0; i < array.length; ++i) {
	        if (key == array[i]) {
	            returnvalue = i;
	            break;
	        }
	    }
	    return returnvalue;
	}
	 
}
