package com.sprockitconsulting.vrslcm.plugin.products;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.sprockitconsulting.vrslcm.plugin.scriptable.BaseLifecycleManagerObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * Container for Product Node(s) which are typically the VMs or Appliances used in the Product.
 * @author justin
 *
 */
@VsoObject(description = "Represents a node/server used in a particular Product in LCM. Typically, this is a VM.")
@VsoFinder( //Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
	description = "Represents a Product Node in LCM.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	name = "ProductNode",
	image = "images/connection.png" // relative path to image in inventory use
)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductNode extends BaseLifecycleManagerObject {
	
	private String type;
	private String name;
	
	public ProductNode() {
		
	}
	
	@JsonUnwrapped
	@JsonProperty("properties")
	private ProductNodeSpec productNodeSpec;
	
	@VsoProperty(description = "The VM name of the node.")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@VsoProperty(description = "The type of node. Examples are 'vidm-connector' or 'vrava-primary' - this value differs per product.")
	public String getType() {
		return type;
	}
	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	@VsoProperty(description = "Specifications of the Node. Use the attached methods to get specific information about the node.")
	public ProductNodeSpec getProductNodeSpec() {
		return productNodeSpec;
	}
	
	public void setProductNodeSpec(ProductNodeSpec productNodeSpec) {
		this.productNodeSpec = productNodeSpec;
	}
	
	
}
