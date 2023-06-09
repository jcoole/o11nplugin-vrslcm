package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprockitconsulting.vrslcm.plugin.services.CredentialService;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
/**
 * Represents a vCenter instance in a Datacenter in LCM.
 * @author justin
 */
@JsonIgnoreProperties({"vCDataCenters", "templateCustomSpecs", "contentLibraries"})
@VsoObject(description = "Represents a vCenter Server in vRSLCM.")
//Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
@VsoFinder(
	name = "VirtualCenter", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "A vRSLCM vCenter configuration.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/vc-sdk.png" // relative path to image in inventory use
)
public class VirtualCenter extends BaseLifecycleManagerObject {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(VirtualCenter.class);
	
	public String name;
	public String host;
	public String user;
	public String lockerReference; // LockerReference ID.
	//public Credential lockerCredential; // The link to the Credential. TODO: remove this
	public String usedAs;
	public String dataCollectionStatus;

	// These are other properties retrieved when you get the object directly.
	public String version = "unsure";

	public VirtualCenter() {}
	
	@VsoProperty(description = "The vCenter Display Name in vRSLCM")
	public String getName() {
		return name;
	}
	@JsonProperty("vCenterName")
	@JsonAlias("name")
	public void setName(String name) {
		this.name = name;
	}
	@VsoProperty(description = "The vCenter Host DNS address")
	public String getHost() {
		return host;
	}
	@JsonProperty("vCenterHost")
	public void setHost(String host) {
		this.host = host;
		super.setResourceId(host);
	}
	@VsoProperty(description = "The vCenter Service User")
	public String getUser() {
		return user;
	}
	@JsonProperty("vcUsername")
	public void setUser(String user) {
		this.user = user;
	}
	
	@VsoProperty(description = "The Locker Reference to the Credential being used for vCenter Host access")
	public String getLockerReference() {
		return lockerReference;
	}
	
	@JsonProperty("vcPassword")
	public void setLockerReference(String reference) {
		this.lockerReference = reference;
	}
	
	@VsoProperty(description = "The Credential used for vCenter Authentication.")	
	public Credential getLockerCredential() {
		log.debug("inside getLockerCredential");
		// convert this to a dynamic lookup to the cred service. does this even need a field?
		Credential lockerCredential = new LockerReference(this.getLockerReference()).getReferencedLockerResource();
		log.debug("got cred: "+lockerCredential.toString());
		return lockerCredential; 
		//return lockerCredential;
	}

	@VsoProperty(description = "The vCenter role - Management, Workload, or Both")
	public String getUsedAs() {
		return usedAs;
	}
	@JsonProperty("vcUsedAs")
	public void setUsedAs(String usedAs) {
		this.usedAs = usedAs;
	}
	
	@VsoProperty(description = "The vCenter's latest Data Collection status.", readOnly = true)
	public String getDataCollectionStatus() {
		return dataCollectionStatus;
	}
	
	@JsonProperty("vcDataCollectionStatus")
	@JsonAlias("dataCollectionStatus")
	public void setDataCollectionStatus(String dataCollectionStatus) {
		this.dataCollectionStatus = dataCollectionStatus;
	}
	
	// TODO: Revisit
	@VsoMethod(description = "Update the vCenter Connection credential")
	public void updateVirtualCenterPassword() {
		throw new RuntimeException("updateVirtualCenterPassword() is not implemented!!");
	}

}
