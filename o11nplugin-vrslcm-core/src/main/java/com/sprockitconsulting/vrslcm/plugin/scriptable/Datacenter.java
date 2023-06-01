package com.sprockitconsulting.vrslcm.plugin.scriptable;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.o11n.plugin.sdk.annotation.Cardinality;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;

/**
 * Represents a Datacenter object in vRSLCM.
 * @author justin
 */
@Component
@Scope(value = "prototype")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@VsoObject(description = "Represents a Datacenter object in vRSLCM.")
//Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
@VsoFinder(
	name = "Datacenter", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "A vRSLCM Datacenter configuration.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup. defined in superclass
	image = "images/vrslcm-dc.png", // relative path to image in inventory use
	relations = {
		@VsoRelation(name = "VirtualCenters", type = "VirtualCenter", inventoryChildren = true, cardinality = Cardinality.TO_MANY)
	}
)
public class Datacenter extends BaseLifecycleManagerObject {
	
	private String name;
	private String location = "Unspecified";
	private List<Environment> environments;
	private List<VirtualCenter> virtualCenters;

	public Datacenter() {

	}
	
	public Datacenter(String name) {
		this.name = name;
	}

	public Datacenter(String name, String location) {
		this.name = name;
		this.location = location;
	}

	@VsoProperty(description = "Name of the Datacenter.")
	public String getName() {
		return name;
	}
	
	@JsonProperty("dataCenterName")
	public void setName(String name) {
		this.name = name;
	}

	@VsoProperty(description = "Location of the Datacenter.")
	public String getLocation() {
		return location;
	}
	
	@JsonProperty("primaryLocation")
	public void setLocation(String location) {
		this.location = location;
	}
	
	@JsonProperty("dataCenterVmid")
	@Override
	public void setResourceId(String resourceId) {
		super.setResourceId(resourceId);
	}


	@VsoProperty(description = "Environments associated with the Datacenter.")
	public List<Environment> getEnvironments() {
		return environments;
		
	}

	public void setEnvironments(List<Environment> environments) {
		this.environments = environments;
	}
	
	@VsoProperty(description = "vCenter Instances associated with the Datacenter.", showInColumn = false, showInDescription = false)
	public List<VirtualCenter> getVirtualCenters() throws Exception {
		return virtualCenters;
	}

	public void setVirtualCenters(List<VirtualCenter> virtualCenters) {
		this.virtualCenters = virtualCenters;
	}
}
