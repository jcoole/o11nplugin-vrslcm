package com.sprockitconsulting.vrslcm.plugin.endpoints;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sprockitconsulting.vrslcm.plugin.scriptable.ConnectionInfo;

import ch.dunes.vso.sdk.endpoints.IEndpointConfiguration;
import ch.dunes.vso.sdk.endpoints.IEndpointConfigurationService;

/**
 * ConnectionPersister is used to perform CRUD operations on Connections stored in the Orchestrator EndpointConfigurationService, and notify other classes when changes are made..
 * It also performs some validations, such as ensuring ID exists, there aren't duplicate endpoints, etc.
 * This is mostly taken from available VMware docs but with additional info and context.
 * @author justin/VMware SDK
 * @linkplain Plug-In SDK Guide for Orchestrator
 */

@Component
public class ConnectionPersister implements IEndpointPersister {

	// Injects the Orchestrator service needed to store Connections in the database.
	// The Connection is turned into a Orchestrator ResourceElement you can review yourself.
	@Autowired
	private IEndpointConfigurationService endpointConfigurationService;
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ConnectionPersister.class);

	// Setup Event Listeners - any class implementing the interface can be added to this collection.
	private final Collection<IConfigurationChangeListener> listeners;
	
	public ConnectionPersister() {
		listeners = new CopyOnWriteArrayList<>();
		log.debug("Constructor initialized with new listeners");
	}

	/**
	 * Interface method from IEndpointPersister
	 * 
	 * This method returns all LCM connections inside of Orchestrator and converts them to a ConnectionInfo object.
	 * 
	 * @return List of ConnectionInfo objects.
	 */
	@Override
	public List<ConnectionInfo> findAll() {
		Collection<IEndpointConfiguration> configs;
		try {
			configs = endpointConfigurationService.getEndpointConfigurations();
			List<ConnectionInfo> result = new ArrayList<>(configs.size());

			// Loop through the stored configurations and convert to ConnectionInfo
			for (IEndpointConfiguration config : configs) {
				ConnectionInfo connectionInfo = getConnectionInfo(config);

				if (connectionInfo != null) {
					log.debug("Adding ConnectionInfo to list: " + connectionInfo);
					result.add(connectionInfo);
				}
			}
			return result;
		} catch (IOException e) {
			log.debug("Error reading connections :: ", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Interface method from IEndpointPersister
	 * 
	 * This method retrieves the specified LCM ConnectionInfo by its unique ID in Orchestrator.
	 * @param id The Connection ID.
	 *
	 * @return The ConnectionInfo object.
	 */
	@Override
	public ConnectionInfo findById(String id) {
		if(id == null || id == "") {
			throw new NullPointerException("ConnectionInfo ID cannot be null or empty!");
		}
		IEndpointConfiguration endpointConfiguration;
		try {
			// Use the configuration service to retrieve the Connection by its ID
			endpointConfiguration = endpointConfigurationService.getEndpointConfiguration(id.toString());
			log.debug("findById("+id+") got EndpointConfiguration successfully.");
			return getConnectionInfo(endpointConfiguration);
		} catch (IOException e) {
			log.debug("Error finding connection by id: " + id.toString(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method takes an IEndpointConfiguration from Orchestrator and converts it into a LCM ConnectionInfo object.
	 * @param config The Endpoint Configuration object
	 * @return The LCM ConnectionInfo object
	 */
	private ConnectionInfo getConnectionInfo(IEndpointConfiguration config) {
		ConnectionInfo info = null;
		try {
			// Test for a valid ID and construct
			info = new ConnectionInfo(config.getId());
			info.setName(config.getString("name"));
			info.setHost(config.getString("host"));
			info.setUserName(config.getString("userName"));
			info.setUserPassword(config.getPassword("userPassword"));
			
			// if domain isn't present, put in local
			if(config.getString("userDomain") == null) {
				info.setUserDomain("local");
			} else {
				info.setUserDomain(config.getString("userDomain"));
			}
			
			
			// Identity Manager values - if the identityManagerHost is not blank, load the values into the ConnectionInfo.
			if(config.getString("identityManagerHost") != null) {
				info.setIdentityManagerHost(config.getString("identityManagerHost"));
				info.setIdentityManagerClientId(config.getString("identityManagerClientId"));
				info.setIdentityManagerClientSecret(config.getPassword("identityManagerClientSecret"));
			}
		} catch (Exception e) {
			log.warn("Unable to convert Orchestrator EndpointConfiguration object to ConnectionInfo: "+config.getId()+", confirm it is valid!", e);
		}
		return info;
	}


	/**
	 * This method adds LCM ConnectionInfo to an IEndpointConfiguration object that will be then stored inside of Orchestrator.
	 * @param config The Endpoint Configuration object
	 * @param info The LCM ConnectionInfo object
	 * @see String#toLowerCase() save
	 */
	private void addConnectionInfoToConfig(IEndpointConfiguration config, ConnectionInfo info) {
		try {
			if(info.getId() == null || info.getId() == "") {
				throw new NullPointerException("ConnectionInfo ID cannot be null or empty!");
			}
			config.setString("id", info.getId().toString());
			config.setString("name", info.getName());
			config.setString("host", info.getHost());
			config.setString("userName", info.getUserName());
			config.setPassword("userPassword", info.getUserPassword());
			
			// if userdomain somehow missing, assume local
			if(info.getUserDomain() == null) {
				config.setString("userDomain", "local");
			} else {
				config.setString("userDomain", info.getUserDomain() );
			}

			// Identity Manager settings. If the identityManagerHost isn't populated, do not add to config.
			// The platform will throw errors about the 'string too short' otherwise!
			if(info.getIdentityManagerHost() != null) {
				config.setString("identityManagerHost", info.getIdentityManagerHost());
				config.setString("identityManagerClientId", info.getIdentityManagerClientId());
				config.setPassword("identityManagerClientSecret", info.getIdentityManagerClientSecret());				
			} 


		} catch (Exception e) {
			log.error("Error converting ConnectionInfo to IEndpointConfiguration :: ", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method saves/updates information for a given Connection.
	 * @param info The Connection information to use in the save/update operation.
	 * @return The Connection info
	 */
	@Override
	public ConnectionInfo save(ConnectionInfo info) {
		// Input validation for object, then ID and Name.
		if(info == null) {
			throw new NullPointerException("ConnectionInfo cannot be null or empty!");
		}

		if(info.getId() == null || info.getId() == "") {
			throw new NullPointerException("ConnectionInfo ID cannot be null or empty!");
		}
		
		// Ensure no duplicate named entries.
		validateConnectionName(info);
		
		try {
			// Check the endpointConfiguration for a Connection with an existing ID.
			// If it exists, it's an update operation, otherwise it is a new one and our process must create the unique ID value.
			IEndpointConfiguration endpointConfiguration = endpointConfigurationService.getEndpointConfiguration(info.getId().toString());
			
			if (endpointConfiguration == null) {
				// Create a new IEndpointConfiguration with the specific ID.
 				endpointConfiguration = endpointConfigurationService.newEndpointConfiguration(info.getId().toString());
			}

			// Convert ConnectionInfo into IEndpointConfiguration
			addConnectionInfoToConfig(endpointConfiguration, info);

			// Use the configuration service to save the endpoint configuration in Orchestrator.
			endpointConfigurationService.saveEndpointConfiguration(endpointConfiguration);

			// Fire an event to all subscribers, that we have updated a configuration.
			// Any class that implements the ConfigurationChangeListener interface can 'do' something with this, like the ConnectionRepository.
			fireConnectionUpdated(info);

		} catch (IOException e) {
			log.error("Error saving ConnectionInfo :: " + info, e);
		}
		return info;
	}

	/**
	 * This method will delete the specified Connection from Orchestrator.
	 * @param info The Connection Info, containing the ID to delete.
	 */
	@Override
	public void delete(ConnectionInfo info) {
		try {
			// Use the configuration service to delete the connection info. The service uses the ID
			endpointConfigurationService.deleteEndpointConfiguration(info.getId().toString());

			// Fire an event to all subscribers, that we have deleted a configuration.
			// Pass the entire connectionInfo object and let the subscribers decide if they need to do something
			fireConnectionRemoved(info);
		} catch (IOException e) {
			log.error("Error deleting endpoint configuration: " + info, e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This method checks for duplicates in the EndpointConfiguration service.
	 * If an existing connection is found by name with a *different* ID, an exception is thrown.
	 * @param info The Connection Info to check.
	 * @see String#toLowerCase() getConfigurationByName
	 */
	private void validateConnectionName(ConnectionInfo info) {
		ConnectionInfo connectionByName = getConnectionByName(info.getName());
		if (connectionByName != null && !connectionByName.getId().toString().equals(info.getId().toString())) {
			throw new RuntimeException("Connection with the same name already exists: " + info);
		}
	}

	/**
	 * This method retrieves the ConnectionInfo based on input name.
	 * @param name The name of the configuration in Orchestrator.
	 * @return The ConnectionInfo
	 * @see String#toLowerCase() validateConnectionName
	 */
	private ConnectionInfo getConnectionByName(String name) {
		if(name == null || name == "") {
			throw new NullPointerException("Connection name cannot be null!");
		}

		Collection<ConnectionInfo> findAllConns = findAll();
		for (ConnectionInfo info : findAllConns) {
			if (name.equals(info.getName())) {
				return info;
			}
		}
		return null;
	}


	/**
	 * Load the configuration of the plugins' connections, and fires an event to anything listening.
	 * This is called by the ConnectionRepository during startup so that any Connections are ready to go at startup.
	 * @see ConnectionRepository#afterPropertiesSet()
	 */
	@Override
	public void load() {
		List<ConnectionInfo> findAll = findAll();
		
		for (ConnectionInfo connectionInfo : findAll) {
			fireConnectionUpdated(connectionInfo);
		}
	}

	/**
	 * Attach a configuration listener.
	 */
	@Override 
	public void addChangeListener(IConfigurationChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * A helper method which iterates all event subscribers and fires the update notification for the provided connection info.
	 */
	private void fireConnectionUpdated(ConnectionInfo connectionInfo) {
		for (IConfigurationChangeListener li : listeners) {
			li.connectionUpdated(connectionInfo);
		}
	}

	/**
	 * A helper method which iterates all event subscribers and fires the delete notification for the provided connection info.
	 */
	private void fireConnectionRemoved(ConnectionInfo connectionInfo) {
		for (IConfigurationChangeListener li : listeners) {
			li.connectionRemoved(connectionInfo);
		}
	}

}
