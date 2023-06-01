package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionRepository;
import com.sprockitconsulting.vrslcm.plugin.services.*;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * This class is the basis for all objects in the system delivered by ObjectFactory.
 * All objects will have the necessary connection, resource, and internal ID values needed for lookups in the plugin.
 * @author justin
 * @see ObjectFactory
 */
@Component
@Scope(value = "prototype")
public abstract class BaseLifecycleManagerObject {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(BaseLifecycleManagerObject.class);
	
	@Autowired
	protected ConnectionRepository repository;
	@Autowired
	protected ApplicationContext context;
	
	protected Connection connection;
	protected String internalId;
	protected String resourceId;

	public BaseLifecycleManagerObject() {
		
	}

	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}
	
	@VsoProperty(description = "ID of the resource in the remote system.", readOnly=true)
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
    @VsoProperty(description = "Connection associated with the resource.", readOnly=true)
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}
    
	// Helper methods for subclasses to extract services they need on demand
	protected EnvironmentService getEnvironmentService() {
		return (EnvironmentService) context.getBean("environmentService", this.connection);
	}
	protected DatacenterService getDatacenterService() {
		return (DatacenterService) context.getBean("datacenterService", this.connection);
	}
	protected RequestService getRequestService() {
		return (RequestService) context.getBean("RequestService", this.connection);
	}
}
