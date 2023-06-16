package com.sprockitconsulting.vrslcm.plugin.scriptable;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoConstructor;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
/**
 * Represents a vCenter instance in a Datacenter in LCM.
 * @author justin
 */
@JsonIgnoreProperties(ignoreUnknown = true) // during deserialization, if a field isn't mapped to a JSON property, skip it
@JsonInclude(JsonInclude.Include.NON_NULL) // during serialization, ignores fields set to null (helps on inherited fields and fields not needed during creation)
@VsoObject(description = "Represents a vCenter Server in vRSLCM.")
//Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
@VsoFinder(
	name = "VirtualCenter", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "A vRSLCM vCenter configuration.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/vc-sdk.png" // relative path to image in inventory use
)
public class VirtualCenter extends BaseLifecycleManagerObject {
	
	private String name;
	private String host;
	private String user;
	private String lockerReference; // LockerReference ID.
	private Credential lockerCredential; // The link to the Credential.
	private Datacenter datacenter; // the associated datacenter object
	private String usedAs;
	private String dataCollectionStatus;

	@VsoConstructor
	public VirtualCenter() {}
	
	@VsoConstructor
	public VirtualCenter(
			@VsoParam(description = "The vCenter Display Name in vRSLCM")String name, 
			@VsoParam(description = "The vCenter Host DNS address")String host, 
			@VsoParam(description = "The Credential used for vCenter Authentication.")Credential lockerCredential, 
			@VsoParam(description = "The vCenter role - Management, Workload, or Both")String usedAs) {
		this.name = name;
		this.host = host;
		this.lockerCredential = lockerCredential;
		this.lockerReference = "locker:password:"+lockerCredential.getResourceId()+":"+lockerCredential.getAlias();
		this.usedAs = usedAs;
	}
	
	@VsoProperty(description = "The vCenter Display Name in vRSLCM")
	public String getName() {
		return name;
	}
	@JsonProperty("vCenterName")
	@JsonAlias("name")
	public void setName(String name) {
		this.name = name;
		// Resource IDs for vCenter objects do not exist in vRSLCM API - only named entities.
		// Since you can technically add as many vCenter connections to the SAME endpoint, the friendly name must be used as the 'resourceId' for lookup purposes.
		super.setResourceId(name);
	}
	@VsoProperty(description = "The vCenter Host DNS address")
	public String getHost() {
		return host;
	}
	@JsonProperty("vCenterHost")
	public void setHost(String host) {
		this.host = host;
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
		return lockerCredential;
	}
	
	public void setLockerCredential(Credential lockerCredential) {
		this.lockerCredential = lockerCredential;
	}
	
	@VsoProperty(description = "The Datacenter associated with the vCenter connection.")
	public Datacenter getDatacenter() {
		return datacenter;
	}

	public void setDatacenter(Datacenter datacenter) {
		this.datacenter = datacenter;
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
