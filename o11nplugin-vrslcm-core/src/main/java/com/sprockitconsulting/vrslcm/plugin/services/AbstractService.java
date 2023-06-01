package com.sprockitconsulting.vrslcm.plugin.services;

import org.springframework.stereotype.Service;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
/**
 * Base class for Services.
 * The empty constructor is used on the singleton services (Datacenter, Environment, Request, etc)
 * The Connection constructor is used by Orchestrator's front end services (LifecycleOperations, Locker) to pass in the relevant Connection to those services and perform API calls.
 * 
 * @author justin
 */
@Service
public abstract class AbstractService {
	
	protected Connection connection;

	public AbstractService() {
		
	}
	public AbstractService(Connection connection) {
		this.connection = connection;
	}

	@VsoProperty(description = "The connection associated to the service.")
	protected Connection getConnection() {
		return connection;
	}
	
	@Override
	public String toString() {
		return String.format("AbstractService [connection=%s]", connection);
	}
	
}
