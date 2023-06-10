package com.sprockitconsulting.vrslcm.plugin.services;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.sprockitconsulting.vrslcm.plugin.dao.DaoRequest;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
/**
 * This service governs access to LCM Requests.
 * It is autowired into the LifecycleOperationsService, used in Orchestrator for users to manage objects.
 * @author justin
 */
@Service
public class RequestService extends AbstractService {
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(RequestService.class);
	
	@Autowired
	private DaoRequest dao;
	
	public RequestService() {}
	
	@Deprecated
	public RequestService(Connection connection) {
		super(connection);
		log.debug("Initialized with Connection ["+connection.getId()+"]");
	}

	public Request getByValue(Connection connection, String value) {
		return dao.findById(connection, value);
	}

	public List<Request> getAll(Connection connection) {
		return dao.findAll(connection);
	}

}
