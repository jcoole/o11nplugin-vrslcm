package com.sprockitconsulting.vrslcm.plugin.products;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.sprockitconsulting.vrslcm.plugin.services.EnvironmentService;
import com.vmware.o11n.plugin.sdk.annotation.Cardinality;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;

/**
 * Represents a vRealize/Aria Automation product.
 * @author justin
 *
 */

@Component
@Scope(value = "prototype")
@JsonTypeName("vra")
@JsonIgnoreProperties(ignoreUnknown = true) // if a field isn't mapped to a JSON property, skip it
@VsoObject(description = "Represents a vRealize/Aria Automation Product object in LCM.")
@VsoFinder( //Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
	name = "AutomationProduct", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Represents a vRealize/Aria Automation Product object in LCM.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/vra32.png", // relative path to image in inventory use
	relations = {
		@VsoRelation(name = "ProductNodes", type = "ProductNode", inventoryChildren = true, cardinality = Cardinality.TO_MANY)
	}
)
public class AutomationProduct extends BaseProduct implements IPowerCycleSupport {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(AutomationProduct.class);
	
	@Autowired
	public AutomationProduct(EnvironmentService environmentService) {
		super(environmentService);
		this.setName("vRealize/Aria Automation");
		this.setProductId("vra");
	}

	@Override
	@VsoMethod(description = "Request that the system power on the Automation product and its related components. This action will also automatically kick off the deploy script.")
	public Request powerOn() {
		log.debug("powerOn() this - "+this.getConnection()+", "+this.getEnvironmentId()+", "+this.getProductId());
		return environmentService.executePowerOn(this.getConnection(), this.getEnvironmentId(), this.getProductId());
	}

	@Override
	@VsoMethod(description = "Request that the system power off the Automation product and its related components. This action will first SSH into the system to gracefully shutdown the pods before Powering off.")
	public Request powerOff() {
		log.debug("powerOff() this - "+this.getConnection()+", "+this.getEnvironmentId()+", "+this.getProductId());
		log.debug(environmentService.ping());
		return environmentService.executePowerOff(this.getConnection(), this.getEnvironmentId(), this.getProductId());
	}

	@Override
	public String toString() {
		return String.format(
				"AutomationProduct [getProductId()=%s, getEnvironmentId()=%s, getName()=%s, getResourceId()=%s, getConnection()=%s]",
				getProductId(), getEnvironmentId(), getName(), getResourceId(), getConnection());
	}


}
