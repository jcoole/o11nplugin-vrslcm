package com.sprockitconsulting.vrslcm.plugin.services;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Datacenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

/**
 * This class is a front-end used by the Connection object in Orchestrator as a means to communicate with the core ObjectFactory.
 * When initialized by the Connection class, the Connection ID is used to to also get the ObjectFactory instance.
 * 
 * This service handles Lifecycle Operations section specific content.
 * @author justin
 */
@VsoObject(description = "Enables access to Lifecycle Operations specific methods for the server connection.")
public class LifecycleOperationsService extends AbstractService {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(LifecycleOperationsService.class);
	
	public LifecycleOperationsService() {
		super();
	}
	
	@VsoMethod(description = "Creates a new Datacenter with the given name and location.")
	public Datacenter createDatacenter(String name, String location) {
		return objectFactory.createDatacenter(name, location);
	}
	
	@VsoMethod(description = "Updates an existing Datacenter's name and/or location.")
	public Datacenter updateDatacenter(Datacenter dc, String name, String location) {
		return objectFactory.updateDatacenter(dc, name, location);
	}
	
	@VsoMethod(description = "Retrieves all Datacenters for this server.")
	public List<Datacenter> getAllDatacenters() {
		return Arrays.asList(objectFactory.getAllDatacenters());
	}
	
	@VsoMethod(description = "Retrieves a Datacenter by name or resource ID on this server.")
	public Datacenter getDatacenterByValue(String nameOrId) {
		return objectFactory.getDatacentersByNameOrId(nameOrId);
	}
	
	@VsoMethod(description = "Deletes a Datacenter on the server. Returns a request to monitor for completion.")
	public Request deleteDatacenter(Datacenter dc) {
		return objectFactory.deleteDatacenter(dc);
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
