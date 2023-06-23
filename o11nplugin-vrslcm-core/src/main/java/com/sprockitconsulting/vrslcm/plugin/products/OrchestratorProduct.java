package com.sprockitconsulting.vrslcm.plugin.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sprockitconsulting.vrslcm.plugin.services.EnvironmentService;
import com.vmware.o11n.plugin.sdk.annotation.Cardinality;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;

/**
 * Represents a vRealize/Aria Orchestrator product.
 * @author justin
 *
 */

@Component
@Scope(value = "prototype")
@JsonTypeName("vro")
@JsonIgnoreProperties(ignoreUnknown = true) // if a field isn't mapped to a JSON property, skip it
@VsoObject(description = "Represents a vRealize/Aria Orchestrator Product object in LCM.")
@VsoFinder( //Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
	name = "OrchestratorProduct", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Represents a vRealize/Aria Orchestrator Product object in LCM.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/vro32.png", // relative path to image in inventory use
	relations = {
		@VsoRelation(name = "ProductNodes", type = "ProductNode", inventoryChildren = true, cardinality = Cardinality.TO_MANY)
	}
)
public class OrchestratorProduct extends BaseProduct {

	@Autowired
	public OrchestratorProduct(EnvironmentService environmentService) {
		super(environmentService);
		this.setName("vRealize/Aria Orchestrator");
		this.setProductId("vro");
	}

	@Override
	public String toString() {
		return String.format(
				"OrchestratorProduct [internalId=%s, productId()=%s, productVersion()=%s]",
				internalId, getProductId(), getProductVersion());
	}


}
