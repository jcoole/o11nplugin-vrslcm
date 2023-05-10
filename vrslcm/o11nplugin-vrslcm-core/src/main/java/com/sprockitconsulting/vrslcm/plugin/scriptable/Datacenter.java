/**
 *
 */
package com.sprockitconsulting.vrslcm.plugin.scriptable;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * Represents a Datacenter object in vRSLCM.
 * @author justin
 */

@VsoObject(description = "Represents a Datacenter object in vRSLCM.")
//Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
@VsoFinder(
	name = "Datacenter", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "A vRSLCM Datacenter configuration.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup. defined in superclass
	image = "images/vrslcm-dc.png" // relative path to image in inventory use
)
public class Datacenter extends BaseLifecycleManagerObject {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(Datacenter.class);

	public String name;
	public String location = "Unspecified";

	public Datacenter() {
		super();
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

	@VsoMethod(description = "Environments associated with the Datacenter.")
	public ArrayList<Environment> getEnvironments() throws Exception {
		throw new Exception("Implement getEnvironments()!!");
	}

	@VsoMethod(description = "vCenter Instances associated with the Datacenter.")
	public ArrayList<VirtualCenter> getVirtualCenters() throws Exception {
		throw new Exception("Implement getVirtualCenters()!!");
	}

	@VsoMethod(description = "Updates a Datacenter's name/location.")
	public void update() throws Exception {
		throw new Exception("Implement update()!!");
	}
}
