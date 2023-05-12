package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * Represents an Environment in LCM.
 * @author justin
 */
// TODO: Revisit this ignore list, may be better to build up the list than make exceptions.
@JsonIgnoreProperties({"logHistory","infrastructure","products","metaData","transactionId","state","environmentData","dataCenterVmid","dataCenterName"})
@VsoObject(description = "Represents a vRSLCM Environment, containing Products and their Nodes/Servers.")
//Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
@VsoFinder(
	name = "Environment", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "A vRSLCM Environment configuration.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/vrslcm-env.png" // relative path to image in inventory use
)
public class Environment extends BaseLifecycleManagerObject {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(Environment.class);

	public String name;
	public String description;
	public String status;
	public String health;
	public String tenant;

	public Environment() {
		super();
	}
	
	@VsoProperty(description = "Name of the Environment")
	public String getName() {
		return name;
	}
	@VsoProperty(description = "ID of the Environment", readOnly = true)
	public String getResourceId() {
		return resourceId;
	}
	@VsoProperty(description = "Environment Description field")
	public String getDescription() {
		return description;
	}
	@VsoProperty(description = "Environment status", readOnly = true)
	public String getStatus() {
		return status;
	}
	@VsoProperty(description = "Environment health", readOnly = true)
	public String getHealth() {
		return health;
	}
	@VsoProperty(description = "Environment tenant", readOnly = true)
	public String getTenant() {
		return tenant;
	}
	
	@JsonProperty("environmentName")
	public void setName(String name) {
		this.name = name;
	}
	
	@JsonProperty("environmentId")
	@JsonAlias("vmid")
	@Override
	public void setResourceId(String id) {
		this.resourceId = id;
	}
	
	@JsonProperty("environmentDescription")
	public void setDescription(String description) {
		this.description = description;
	}
	
	@JsonProperty("environmentStatus")
	@JsonAlias("status")
	public void setStatus(String status) {
		this.status = status;
	}

	@JsonProperty("environmentHealth")
	public void setHealth(String health) {
		this.health = health;
	}
	
	@JsonProperty("tenant")
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	@VsoMethod(description = "Trigger Inventory Sync on the Environment.")
	public void triggerInventorySync() {
		throw new RuntimeException("triggerInventorySync is not implemented!");
	}

	@Override
	public String toString() {
		return String.format("Environment [name=%s, description=%s, status=%s, health=%s, tenant=%s, internalId=%s]",
				name, description, status, health, tenant, internalId);
	}





}