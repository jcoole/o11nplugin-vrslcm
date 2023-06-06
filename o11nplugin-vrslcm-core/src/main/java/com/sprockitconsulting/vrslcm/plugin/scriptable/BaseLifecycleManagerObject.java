package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionRepository;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * This class is the basis for all objects in the system.
 * All objects will have the necessary connection, resource, and internal ID values needed for lookups in the plugin.
 * @author justin
 */
@Component
@Scope(value = "prototype")
public abstract class BaseLifecycleManagerObject {
	
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
}
