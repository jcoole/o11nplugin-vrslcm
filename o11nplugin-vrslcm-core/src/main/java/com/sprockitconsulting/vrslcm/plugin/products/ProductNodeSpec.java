package com.sprockitconsulting.vrslcm.plugin.products;

import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;

import java.util.HashMap;
import java.util.List;
/**
 * Container for Node-level properties.
 * This class is used primarily for deserialization and exposing generic methods for Workflow developers to key off of.
 * @author justin
 *
 */
@VsoObject(description = "Specifications of the Product Node.  The specifications differ for each Product and Node type, so use the methods attached to find what you're looking for.", create = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductNodeSpec {
	Map<String, Object> properties = new HashMap<>();
	
	@VsoMethod(description = "Retrieves a Product Node property by name.")
	public Object getNodeProperty(@VsoParam(description = "Name of the property to get the value of from the Product Node specifications.")String name) {
		return properties.get(name);
	}
	
	@VsoMethod(description = "Retrieves a list of Property names for the Product Node. Use this to discover what properties are available for you to key off of, using the @{getNodeProperty} method.")
	public List<String> getNodePropertyNames() {
		return properties.keySet().stream().collect(Collectors.toList());
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	@JsonAnySetter
	public void setProperties(String key, Object value) {
		properties.put(key, value);
	}
}
