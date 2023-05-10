package com.sprockitconsulting.vrslcm.plugin.scriptable;
import java.time.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * Represents a Request object in LCM.
 * @author justin
 */
@JsonIgnoreProperties(value = {"lastUpdatedOnDateTime"} )
@VsoObject(description = "Represents a Request in vRSLCM.", create = false)
@VsoFinder( //Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
	name = "Request", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "A vRSLCM Request, made by an admin or automated server task.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/vrslcm-request.png" // relative path to image in inventory use
)
public class Request extends BaseLifecycleManagerObject implements Comparable<Request> {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(Request.class);

	private String name;
	private String source;
	private String requestor;
	private long lastUpdated;
	private String tenant;
	private String state;
	private String lastUpdatedOnDateTime;

	public Request() {
		
	}
	
	@VsoProperty(description = "Request Name and Reason", readOnly = true)
	public String getName() {
		return name;
	}
	
	@JsonProperty("requestReason")
	public void setName(String name) {
		this.name = name;
	}
  	
	@Override
	@JsonProperty("vmid")
	@JsonAlias("requestId")
	public void setResourceId(String resourceId) {
		super.setResourceId(resourceId);
	}
	
	@VsoProperty(description = "The source of the request - can be a regular scheduled task or user generated.", readOnly = true)
	public String getSource() {
		return source;
	}
	@JsonProperty("requestSourceType")
	public void setSource(String source) {
		this.source = source;
	}
	
	@VsoProperty(description = "User account that made the request.", readOnly = true)
	public String getRequestor() {
		return requestor;
	}
	
	@JsonProperty("createdBy")
	public void setRequestor(String requestor) {
		this.requestor = requestor;
	}
	
	@VsoProperty(description = "The tenant where the Request originated from.", readOnly = true)
	public String getTenant() {
		return tenant;
	}
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	
	@VsoProperty(description = "The state of the request as of the last updated time.", readOnly = true)
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
	}
	
	@VsoProperty(description = "The time the request was last updated, in seconds since the Unix epoch.", readOnly = true)
	public long getLastUpdated() {
		return lastUpdated;
	}
	
	@JsonProperty("lastUpdatedOn")
	public void setLastUpdated(long lastUpdated) {
		this.lastUpdated = lastUpdated;
		// use this value to populate the datetime field.
		// TODO: investigate timezone conversion based on appliance properties?
		this.lastUpdatedOnDateTime = Instant.ofEpochMilli(lastUpdated).toString();
	}
	
	@VsoProperty(description = "The time the request was last updated, as a readable date.", readOnly = true)
	public String getLastUpdatedOnDateTime() {
		return lastUpdatedOnDateTime;
	}
	
	public void setLastUpdatedOnDateTime(String lastUpdatedOnDateTime) {
		this.lastUpdatedOnDateTime = lastUpdatedOnDateTime;
	}
	
	@VsoMethod(description = "Retry a request (if possible)")
	public static void retry() {
		log.error("Request retry() method not implemented");
	}
	
	@VsoMethod(description = "Get request details for the request")
	public static void getDetails() {
		log.error("Request getDetails() method not implemented");
	}
	
	@VsoMethod(description = "Get request error cause")
	public static void getErrors() {
		log.error("Request getErrors() method not implemented");
	}
	
	/**
	 * Interface method from Comparable.
	 * Requests come in via the API with no sorting mechanism, so this method is used by the ObjectFactory to sort the requests based on the 'lastUpdated' property.
	 * @param o
	 * @return
	 */
	@Override
	public int compareTo(Request o) {
		// Compares 'this' instance to the 'o'ther one.
		return this.getLastUpdated() == o.getLastUpdated() ? 0 : 
			this.getLastUpdated() > o.getLastUpdated() ? 1 : -1;
	}
}
