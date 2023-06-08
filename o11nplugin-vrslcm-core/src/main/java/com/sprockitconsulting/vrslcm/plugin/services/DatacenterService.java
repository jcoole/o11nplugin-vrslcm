package com.sprockitconsulting.vrslcm.plugin.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Datacenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
import com.sprockitconsulting.vrslcm.plugin.scriptable.VirtualCenter;
import com.sprockitconsulting.vrslcm.plugin.dao.DaoDatacenter;
/**
 * This service governs access to the LCM Datacenter resources and related objects.
 * 
 * @author justin
 */
@Service
public class DatacenterService extends AbstractService {
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(DatacenterService.class);
	
	@Autowired
	private DaoDatacenter dao;

	public DatacenterService() {
		log.debug("DatacenterService initialized");
	}

	public Datacenter getByValue(Connection connection, String value) {
		return dao.findById(connection, value);
	}

	public List<Datacenter> getAll(Connection connection) {
		return dao.findAll(connection);
	}

	public List<Environment> findAllEnvironmentsInDatacenter(Connection connection, String nameOrId) {
		return dao.getEnvironments(connection, nameOrId);
	}
	
	public List<VirtualCenter> findAllVirtualCentersInDatacenter(Connection connection, String nameOrId) {
		return dao.getVirtualCenters(connection, nameOrId);
	}
	
	public Datacenter create(Connection connection, Datacenter dc) {
		return dao.create(connection, dc);
	}
	
	public Datacenter update(Connection connection, Datacenter original, Datacenter replacement) {
		Datacenter updatedDc = dao.update(connection, original, replacement);
		return dao.findById(connection, updatedDc.getResourceId());
	}
	
	public void delete(Connection connection, Datacenter dc) {
		dao.delete(connection, dc);
	}
}
