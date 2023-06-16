package com.sprockitconsulting.vrslcm.plugin.products;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;

/**
 * Container for Product-level properties.
 * This class is used primarily for deserialization and exposing generic methods for Workflow developers to key off of.
 * @author justin
 *
 */
@VsoObject(description = "Specifications used in the deployment of the Product. The specifications differ for each Product, so use the methods attached to find what you're looking for.")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSpec {
	
	Map<String, Object> properties = new HashMap<>();
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	@VsoMethod(description = "Retrieves a list of Property names for the Product. Use this to discover what properties are available for you to key off of, using the @{getProductProperty} method.")
	public List<String> getProductPropertyNames() {
		return properties.keySet().stream().collect(Collectors.toList());
	}
	
	@VsoMethod(description = "Retrieves a Product property by name.")
	public Object getProductProperty(@VsoParam(description = "Name of the property to retrieve, such as 'nodeSize'")String name) {
		return properties.get(name);
	}

	@JsonAnySetter
	public void setProperties(String key, Object value) {
		properties.put(key, value);
	}
}
