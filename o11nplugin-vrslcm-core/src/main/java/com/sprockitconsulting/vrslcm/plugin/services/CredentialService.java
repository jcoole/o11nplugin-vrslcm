package com.sprockitconsulting.vrslcm.plugin.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sprockitconsulting.vrslcm.plugin.dao.DaoCredential;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Credential;
/**
 * This service governs access to the LCM Credential resources.
 * @author justin
 *
 */
@Service
public class CredentialService extends AbstractService {
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(CredentialService.class);
	
	@Autowired
	private DaoCredential dao;
	
	public CredentialService() {
		log.debug("CredentialService initialized");
	}

	public Credential getByValue(Connection connection, String value) {
		return dao.findById(connection, value);
	}
	
	public List<Credential> getAll(Connection connection) {
		return dao.findAll(connection);
	}

	public Credential create(Connection connection, Credential credential) {
		return dao.create(connection, credential);
	}
	
	public Credential update(Connection connection, Credential original, Credential replacement) {
		return dao.update(connection, original, replacement);
	}
	
	public void delete(Connection connection, Credential credential) {
		dao.delete(connection, credential);
	}

	@Override
	public String toString() {
		return String.format("CredentialService [dao=%s, connection=%s]", dao, connection);
	}
	
	
}
