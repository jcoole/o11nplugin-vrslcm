package com.sprockitconsulting.vrslcm.plugin.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sprockitconsulting.vrslcm.plugin.dao.DaoVirtualCenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Datacenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.sprockitconsulting.vrslcm.plugin.scriptable.VirtualCenter;
/**
 * This service governs access to the LCM vCenter resources.
 * It has some dependency on the DatacenterService since they are intertwined at the product level.
 * Since the non-GET methods all return Requests - the logic in the workflows should handle waiting for completion and retrieving the updated resources.
 * 
 * @author justin
 */
@Service
public class VirtualCenterService extends AbstractService {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(VirtualCenterService.class);
	
	@Autowired
	private DaoVirtualCenter dao;

	public VirtualCenterService() {
		log.debug("VirtualCenterService initialized");
	}

	public VirtualCenter getByName(Datacenter dc, String name) {
		return dao.findByName(dc, name);
	}

	public List<VirtualCenter> getAll(Connection connection) {
		return dao.findAll(connection);
	}
	
	public Request create(Datacenter dc, VirtualCenter vc) {
		return dao.create(dc, vc);
	}
	
	public Request update(Datacenter dc, VirtualCenter original, VirtualCenter replacement) {
		return dao.update(dc, original.getName(), replacement);
	}
	
	public Request delete(Datacenter dc, String name) {
		return dao.delete(dc, name);
	}
	
	public Request sync(Datacenter dc, String name) {
		return dao.sync(dc, name);
	}
}
