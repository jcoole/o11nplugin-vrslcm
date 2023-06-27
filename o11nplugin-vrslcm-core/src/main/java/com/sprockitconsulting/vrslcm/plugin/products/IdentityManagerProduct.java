package com.sprockitconsulting.vrslcm.plugin.products;

import java.util.Arrays;

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
 * Represents a VMware Identity Manager Product object in LCM.
 * @author justin
 */
@Component
@Scope(value = "prototype")
@JsonTypeName("vidm")
@JsonIgnoreProperties(ignoreUnknown = true) // if a field isn't mapped to a JSON property, skip it
@VsoObject(description = "Represents a VMware Identity Manager Product object in LCM.")
@VsoFinder( //Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
	name = "IdentityManagerProduct", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Represents a VMware Identity Manager Product object in LCM.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/vidm32.png", // relative path to image in inventory use
	relations = {
		@VsoRelation(name = "ProductNodes", type = "ProductNode", inventoryChildren = true, cardinality = Cardinality.TO_MANY)
	}
)
public class IdentityManagerProduct extends AbstractProduct implements IPowerCycleSupport {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(IdentityManagerProduct.class);
	
	@Autowired
	public IdentityManagerProduct() {
		super();
		this.setName("VMware Identity Manager/Workspace ONE Access");
		this.setProductId("vidm");
	}
	
	@Override
	@VsoMethod(description = "Request that the system power on the Identity Manager product and its related components. In a clustered scenario it will also remediate the cluster to ensure everything comes up properly.")
	public Request powerOn() {
		log.debug("Requesting power on of "+this.getInternalId());
		return getEnvironmentService().executePowerOn(this.getConnection(), this.getEnvironmentId(), this.getProductId());
	}

	@Override
	@VsoMethod(description = "Request that the system power off the Identity Manager product and its related components. In a clustered scenario it will also ensure that all components are shut down in the proper order.")
	public Request powerOff() {
		log.debug("Requesting power off of "+this.getInternalId());
		return getEnvironmentService().executePowerOff(this.getConnection(), this.getEnvironmentId(), this.getProductId());
	}
	
	@Override
	public String toString() {
		return String.format(
				"IdentityManagerProduct [connection=%s, internalId=%s, getProductId()=%s, getEnvironmentId()=%s, getProductVersion()=%s, getProductName()=%s, getProductSpec()=%s, getProductNodes()=%s, getConnection()=%s]",
				connection, internalId, getProductId(), getEnvironmentId(), getProductVersion(), getName(),
				getProductSpec(), Arrays.toString(getProductNodes()), getConnection());
	}
}
