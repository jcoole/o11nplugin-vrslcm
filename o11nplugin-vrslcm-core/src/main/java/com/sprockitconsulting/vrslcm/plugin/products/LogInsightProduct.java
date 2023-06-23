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
 * Represents a Log Insight product.
 * @author justin
 *
 */

@Component
@Scope(value = "prototype")
@JsonTypeName("vrli")
@JsonIgnoreProperties(ignoreUnknown = true) // if a field isn't mapped to a JSON property, skip it
@VsoObject(description = "Represents a vRealize Log Insight/Aria Insight for LogsProduct object in LCM.")
@VsoFinder( //Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
	name = "LogInsightProduct", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Represents a vRealize Log Insight/Aria Insight for LogsProduct object in LCM.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/vrli32.png", // relative path to image in inventory use
	relations = {
		@VsoRelation(name = "ProductNodes", type = "ProductNode", inventoryChildren = true, cardinality = Cardinality.TO_MANY)
	}
)
public class LogInsightProduct extends BaseProduct {

	@Autowired
	public LogInsightProduct(EnvironmentService environmentService) {
		super(environmentService);
		this.setName("vRealize/Aria Log Insight");
		this.setProductId("vrli");
	}

	@Override
	public String toString() {
		return String.format(
				"LogInsightProduct [internalId=%s, productId()=%s, productVersion()=%s]",
				internalId, getProductId(), getProductVersion());
	}


}
