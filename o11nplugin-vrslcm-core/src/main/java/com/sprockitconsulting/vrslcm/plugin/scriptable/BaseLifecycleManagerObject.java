package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionRepository;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * This class is the basis for all objects in the system delivered by ObjectFactory.
 * All objects will have the necessary connection, resource, and internal ID values needed for lookups in the plugin.
 * @author justin
 * @see ObjectFactory
 */
public abstract class BaseLifecycleManagerObject {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(BaseLifecycleManagerObject.class);
	
	@Autowired
	protected ConnectionRepository repository;
	
	protected String connectionId;
	protected String internalId;
	protected String resourceId;

	public BaseLifecycleManagerObject() {
		
	}

	@VsoProperty(description = "Connection ID of the resource. Can be used to support lookup functionality.", readOnly = true, showInColumn = false)
	public String getConnectionId() {
		return connectionId;
	}
	
	public void setConnectionId(String connectionId) {
		this.connectionId = connectionId;
	}
	
	@VsoProperty(description = "Internal Plugin ID of the resource. This value is [Resource ID]@[Connection ID] and can be used in relating objects.", readOnly=true)
	public String getInternalId() {
		return internalId;
	}

	public void setInternalId(String internalId) {
		this.internalId = internalId;
	}
	
	@VsoProperty(description = "ID of the resource in the remote system.", readOnly=true, showInColumn = false)
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
/*
	// Revisit this method until it works. Any object should be able to use this, or do lookup by id in ConnectionManager.
	@VsoMethod(description = "The connection associated to this object.")
	public Connection getServerConnection() {
		String id = this.getConnectionId();
		log.debug("Attempting to query repository for Connection ID ["+id+"] from repo ["+repository.toString()+"]");
		
		Connection conn = repository.findLiveConnection(id); // this lookup fails. Autowire failure?
		return conn;
	}
*/
}
