package com.sprockitconsulting.vrslcm.plugin.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sprockitconsulting.vrslcm.plugin.component.ObjectFactory;
import com.sprockitconsulting.vrslcm.plugin.scriptable.BaseLifecycleManagerObject;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;


public abstract class AbstractService {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(AbstractService.class);
	
	public ObjectFactory objectFactory;
	public Connection connection;

	public AbstractService() {
		log.debug("No-arg constructor - assign values");
	}
	
	public ObjectFactory getObjectFactory() {
		return objectFactory;
	}
	public void setObjectFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	@VsoProperty(description = "The connection associated to the service.")
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	@Override
	public String toString() {
		return String.format("AbstractService [objectFactory=%s, connection=%s]", objectFactory, connection);
	}
	
}
