package com.sprockitconsulting.vrslcm.plugin.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This service is used to configure the LCM Server instance itself or perform actions against it.
 * @author justin
 *
 */
@Service
public class ConfigurationService extends AbstractService {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ConfigurationService.class);
	
	//private DaoConfiguration dao;

	public ConfigurationService() {
		log.debug("ConfigurationService initialized");
	}

}
