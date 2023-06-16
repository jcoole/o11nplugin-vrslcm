package com.sprockitconsulting.vrslcm.plugin.scriptable;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;

/**
 * This class is a container for the 'infrastructure.properties' data when returning an Environment from the API.
 * @author justin
 *
 */
@VsoObject(description = "Represents a vRSLCM Environment's Infrastructure properties, such as vSphere Cluster, Datastore, Network, and DNS settings.")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EnvironmentInfrastructure {
	
	private Map<String,Object> properties; // the properties
	
	public EnvironmentInfrastructure() {

	}
	@VsoMethod(description = "Retrieve an infrastructure property value by name.")
	public Object getProperty(@VsoParam(description = "Name of the property to retrieve. The return value could be anything so make sure to add some checks in your script.")String key) {
		return properties.get(key);
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}

	@JsonProperty("properties")
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;

	}

	@Override
	public String toString() {
		return String.format("EnvironmentInfrastructure [properties=%s]", properties.toString());
	}




}
