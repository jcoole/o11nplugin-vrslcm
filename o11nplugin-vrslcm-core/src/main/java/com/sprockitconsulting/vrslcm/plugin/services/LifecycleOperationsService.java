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
import com.sprockitconsulting.vrslcm.plugin.scriptable.VirtualCenter;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;

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
	@Autowired
	private VirtualCenterService virtualCenterService;
	
	public LifecycleOperationsService(Connection connection) {
		super(connection);
		log.debug("Lifecycle Operations Service with Connection ["+connection.getId()+"] initialized");
	}

	@VsoMethod(description = "Creates a new Datacenter with the given name and location.")
	public Datacenter createDatacenter(
			@VsoParam(description = "Name of the Datacenter to create.")String name, 
			@VsoParam(description = "Location of the Datacenter.")String location
			) throws JsonProcessingException {
		return datacenterService.create(connection, new Datacenter(name, location));
	}
	
	@VsoMethod(description = "Updates an existing Datacenter's name and/or location.")
	public Datacenter updateDatacenter(
			@VsoParam(description = "The Datacenter to update.")Datacenter dc, 
			@VsoParam(description = "Updated Datacenter name.")String name, 
			@VsoParam(description = "Updated Datacenter location.")String location
			) {
		return datacenterService.update(connection, dc, new Datacenter(name,location) );
	}
	
	@VsoMethod(description = "Retrieves all Datacenters for this server.")
	public List<Datacenter> getAllDatacenters() {
		return datacenterService.getAll(connection);
	}
	
	@VsoMethod(description = "Retrieves a Datacenter by name or resource ID on this server.")
	public Datacenter getDatacenterByValue(
			@VsoParam(description = "Name or ID of the Datacenter to retrieve.")String nameOrId
			) {
		return datacenterService.getByValue(connection, nameOrId);
	}
	
	@VsoMethod(description = "Deletes a Datacenter on the server. Returns a request to monitor for completion.")
	public void deleteDatacenter(
			@VsoParam(description = "The Datacenter to delete.")Datacenter dc
			) {
		datacenterService.delete(connection, dc);

	}
	
	@VsoMethod(description = "Retrieves all Environments for this server.")
	public List<Environment> getAllEnvironments() {
		return environmentService.getAll(connection);
	}
	
	@VsoMethod(description = "Retrieves an Environment by name or resource ID on this server.")
	public Environment getEnvironmentByValue(
			@VsoParam(description = "Name or ID of the Environment to retrieve.")String nameOrId
			) {
		return environmentService.getByValue(connection, nameOrId);
	}
	
	@VsoMethod(description = "Retrieves all Requests for this server.")
	public List<Request> getAllRequests() {
		return requestService.getAll(connection);
	}
	
	@VsoMethod(description = "Retrieves a Request by resource ID on this server.")
	public Request getRequestById(
			@VsoParam(description = "ID of the Request.")String requestId
			) {
		return requestService.getByValue(connection, requestId);
	}
	
	@VsoMethod(description = "Retrieves a vCenter in a Datacenter by name.")
	public VirtualCenter getVirtualCenter(
			@VsoParam(description = "The Datacenter to search.")Datacenter dc, 
			@VsoParam(description = "Name of the vCenter.")String name
			) {
		return virtualCenterService.getByName(dc, name);
	}
	
	@VsoMethod(description = "Retrieves all vCenter connections on the server.")
	public List<VirtualCenter> getAllVirtualCenters() {
		return virtualCenterService.getAll(connection);
	}
	
	@VsoMethod(description = "Request to update a vCenter's information and properties.")
	public Request updateVirtualCenter(
			@VsoParam(description = "The Datacenter containing the vCenter connection.")Datacenter dc, 
			@VsoParam(description = "The vCenter to update.")VirtualCenter original, 
			@VsoParam(description = "The vCenter object with updated values.")VirtualCenter updated
			) {
		return virtualCenterService.update(dc, original, updated);
	}
	
	@VsoMethod(description = "Request creation of a new vCenter Connection in the Datacenter.")
	public Request createVirtualCenter(
			@VsoParam(description = "The Datacenter to create the vCenter in.")Datacenter dc, 
			@VsoParam(description = "The vCenter object to create.")VirtualCenter vc
			) {
		return virtualCenterService.create(dc, vc);
	}
	
	@VsoMethod(description = "Request deleting a vCenter from the Datacenter. If any Environments are associated with it, the Request will fail!")
	public Request deleteVirtualCenter(
			@VsoParam(description = "The Datacenter containing the vCenter connection to delete.")Datacenter dc, 
			@VsoParam(description = "Name of the vCenter connection to delete.")String name
			) {
		return virtualCenterService.delete(dc, name);
	}
	
	@VsoMethod(description = "Request to perform a data collection on a vCenter.")
	public Request syncVirtualCenter(
			@VsoParam(description = "The Datacenter containing the vCenter connection to sync.")Datacenter dc, 
			@VsoParam(description = "Name of the vCenter to sync.")String name
			) {
		return virtualCenterService.sync(dc, name);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
