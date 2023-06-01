package com.sprockitconsulting.vrslcm.plugin.services;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sprockitconsulting.vrslcm.plugin.dao.DaoDatacenter;
import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionRepository;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Datacenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

/**
 * This service handles Lifecycle Operations section specific content.
 * @author justin
 */
@VsoObject(description = "Enables access to Lifecycle Operations specific methods for the server connection.")
@Component("lifecycleOperationsService")
@Scope(value = "prototype")
public class LifecycleOperationsService extends AbstractService {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(LifecycleOperationsService.class);
	
	@Autowired
	private DatacenterService datacenterService;
	@Autowired
	private EnvironmentService environmentService;
	@Autowired
	private RequestService requestService;
	
	public LifecycleOperationsService(Connection connection) {
		super(connection);
		log.debug("Lifecycle Operations Service with Connection ["+connection.getId()+"] initialized");
	}

	@VsoMethod(description = "Creates a new Datacenter with the given name and location.")
	public Datacenter createDatacenter(String name, String location) throws JsonProcessingException {
		return datacenterService.create(connection, new Datacenter(name, location));
	}
	
	@VsoMethod(description = "Updates an existing Datacenter's name and/or location.")
	public Datacenter updateDatacenter(Datacenter dc, String name, String location) {
		return datacenterService.update(connection, dc, new Datacenter(name,location) );

	}
	
	@VsoMethod(description = "Retrieves all Datacenters for this server.")
	public List<Datacenter> getAllDatacenters() {
		return datacenterService.getAll(connection);
	}
	
	@VsoMethod(description = "Retrieves a Datacenter by name or resource ID on this server.")
	public Datacenter getDatacenterByValue(String nameOrId) {
		return datacenterService.getByValue(connection, nameOrId);
	}
	
	@VsoMethod(description = "Deletes a Datacenter on the server. Returns a request to monitor for completion.")
	public void deleteDatacenter(Datacenter dc) {
		datacenterService.delete(connection, dc);

	}
	
	@VsoMethod(description = "Retrieves all Environments for this server.")
	public List<Environment> getAllEnvironments() {
		return environmentService.getAll(connection);
	}
	
	@VsoMethod(description = "Retrieves an Environment by name or resource ID on this server.")
	public Environment getEnvironmentByValue(String nameOrId) {
		return environmentService.getByValue(connection, nameOrId);
	}
	
	@VsoMethod(description = "Retrieves all Requests for this server.")
	public List<Request> getAllRequests() {
		return requestService.getAll(connection);
	}
	
	@VsoMethod(description = "Retrieves a Request by resource ID on this server.")
	public Request getRequestById(String requestId) {
		return requestService.getByValue(connection, requestId);
	}

}
