package com.sprockitconsulting.vrslcm.plugin.services;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sprockitconsulting.vrslcm.plugin.component.ObjectFactory;
import com.sprockitconsulting.vrslcm.plugin.component.PluginBeanConfiguration;
import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionRepository;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Datacenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

/**
 * This class is a front-end used by the Connection object in Orchestrator as a means to communicate with the core ObjectFactory.
 * The Connection object is passed down so that the ObjectFactory can utilize it.
 * @author justin
 *
 */
@VsoObject(description = "Enables access to Lifecycle Operations specific methods for the server connection.")
public class LifecycleOperationsService extends AbstractService {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(LifecycleOperationsService.class);
	
	public LifecycleOperationsService() {
		super();
	}
		
	@VsoMethod(description = "Retrieves all Datacenters for this server.")
	public List<Datacenter> getAllDatacenters() {
		return Arrays.asList(objectFactory.getAllDatacenters());
	}
	
	@VsoMethod(description = "Retrieves a Datacenter by name or resource ID on this server.")
	public Datacenter getDatacenterByValue(String nameOrId) {
		return objectFactory.getDatacentersByNameOrId(nameOrId);
	}
	
	@VsoMethod(description = "Retrieves all Environments for this server.")
	public List<Environment> getAllEnvironments() {
		return Arrays.asList(objectFactory.getAllEnvironments());
	}
	
	@VsoMethod(description = "Retrieves an Environment by name or resource ID on this server.")
	public Environment getEnvironmentByValue(String nameOrId) {
		return objectFactory.getEnvironmentsByNameOrId(nameOrId);
	}
	
	@VsoMethod(description = "Retrieves all Requests for this server.")
	public List<Request> getAllRequests() {
		return Arrays.asList(objectFactory.getAllRequests());
	}
	
	@VsoMethod(description = "Retrieves a Request by resource ID on this server.")
	public Request getRequestById(String requestId) {
		return objectFactory.getRequestByNameOrId(requestId);
	}
}
