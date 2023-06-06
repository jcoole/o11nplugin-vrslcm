package com.sprockitconsulting.vrslcm.plugin.endpoints;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.ConnectionInfo;

/**
 * Connection Repository is used as a local cache of the plugin for live connections.
 * It implements the IConfigurationChangeListener so that any changes made to configurations are passed through here.
 * In addition it is initialized as a Spring singleton bean at startup, and it is available for auto-wiring to any concrete class.
 * @author justin
 */

@Component
public class ConnectionRepository implements ApplicationContextAware, InitializingBean, IConfigurationChangeListener {
	
	/*
	 * ConnectionPersister is injected here so that the Repository can attach itself to the ConfigurationChangeListener and perform updates as needed.
	 */
	@Autowired
	private ConnectionPersister persister;
	
	/*
	 * ApplicationContext is injected here so that the Repository can create Connection Beans (with constructor arguments) for other classes to use.
	 * These Connection beans are stored in the 'connections' map as they are created.
	 */
	@Autowired
	private ApplicationContext context;
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ConnectionRepository.class);
	
	/**
	 * Local cache of connections, authentications
	 *  
	 */
	private final Map<String, Connection> connections;

	private final Map<String, ConnectionAuthentication> connectionAuthentications;
	
	
	public ConnectionRepository() {
		connections = new ConcurrentHashMap<>();
		connectionAuthentications = new ConcurrentHashMap<>();
		log.debug("vRSLCM Plugin Connection Repository initialized");
	}

	/**
	 * Returns a connection by its ID
	 */
	public Connection findLiveConnection(String id) {
		return connections.get(id);
	}

	/**
	 * Returns all connections from the repository
	 */
	public Collection<Connection> findAll() {
		return connections.values();
	}
	
	/**
	 * Get Authentication by Connection ID
	 */
	public ConnectionAuthentication findConnectionAuthentication(String id) {
		ConnectionAuthentication auth = connectionAuthentications.get(id);
		if(auth != null) {
			if(!auth.isTokenValid()) {
				log.debug("ConnectionAuthentication isTokenValid() set to false, acquiring token");
				try {
					auth.acquireToken();
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				connectionAuthentications.replace(id, auth);
			}
		} else {
			log.debug("ConnectionAuthentication with the given ID ["+id+"] is null, creating a new one.");
			auth = createAuthentication(connections.get(id).getConnectionInfo() );
			connectionAuthentications.put(id, auth);
		}
		return auth; 
	}

	/**
	 * Interface method from ApplicationContextAware
	 * 
	 * Storing a reference to the Spring Context in the repository for Bean manipulation.
	 * Primarily used to create a 'Connection' bean
	 */
	@Override
	public void setApplicationContext(ApplicationContext context) throws BeansException {
		this.context = context;
	}

	/**
	 * Interface method from InitializingBean
	 * 
	 * Implementing this interface gives you the ability to perform changes to Beans *after* startup and *before* they are provided for usage.
	 * In our case, we are doing the following :
	 * 		- Attaching this class to the change listener in ConnectionPersister, so we can monitor for updates and respond to them.
	 * 		- Then, tell the ConnectionPersister to load all Connections from the Orchestrator EndpointConfigurationService.
	 */
	@Override 
	public void afterPropertiesSet() throws Exception {
		persister.addChangeListener(this);
		persister.load();

		log.debug("ConnectionRepository now listening for changes to the ConnectionPersister.");
	}

	/**
	 * This method will create a new bean via the ApplicationContext using the ConnectionInfo.
	 * It leverages the @Component and @Scope "prototype" setup of the Connection class.
	 * @param info The Connection Info to use in creation.
	 * @return Connection object
	 */
	private Connection createConnection(ConnectionInfo info) {
		Connection connectionBean = (Connection) context.getBean("connection", info);
		log.debug("Connection Bean was created with info ["+info.toString()+"]");
		return connectionBean;
	}
	
	/**
	 * Creates a ConnectionAuthentication object for the Connection.
	 * This value contains the token type and value for authorized API requests.
	 */
	private ConnectionAuthentication createAuthentication(ConnectionInfo info) {
		ConnectionAuthentication auth = (ConnectionAuthentication) context.getBean("connectionAuthentication", info);
		log.debug("Connection Authentication for Connection ID ["+info.getId()+"] was created");
		return auth;
	}

	/**
	 * Interface method from ConfigurationChangeListener
	 * 
	 * Called whenever a new Connection is added or updated.
	 * 
	 * In particular, this method will place the newly updated Connection into the 'connections' map, which can be accessed by other classes elsewhere.
	 * 
	 * In addition, this will handle the ObjectFactory in the same way.
	 * 
	 * @param info The ConnectionInfo object sent by the ConfigurationChangeListener.
	 */
	@Override
	public void connectionUpdated(ConnectionInfo info) {
		log.debug("Repository detected Connection update : ["+info.toString()+"]");
		Connection live = connections.get(info.getId());

		if (live != null) {
			// The connection exists in the repository, update.
			live.update(info);
		} else {
			// Connection did not previously exist, so create it and add to the repository.
			live = createConnection(info);
			connections.put(info.getId(), live);
		}
		
		// Update ConnectionAuth (if needed)
		ConnectionAuthentication auth = connectionAuthentications.get(info.getId());
		if(auth != null) {
			try {
				auth.setConnectionInfo(info);
			} catch (JsonProcessingException e) {
				log.error("There was an error assigning the updated info to the Authentication object: "+e.getMessage());
				e.printStackTrace();
			}
			connectionAuthentications.replace(info.getId(), auth);
		} else {
			auth = createAuthentication(info);
			connectionAuthentications.put(info.getId(), auth);
		}
	}

	/**
	 * Interface method from ConfigurationChangeListener
	 * 
	 * Called whenever a vRSLCM Connection is removed.
	 * 
	 * If a removed Connection is found in the HashMap, it is removed, along with related components.
	 * 
	 * @param info The ConnectionInfo object sent by the ConfigurationChangeListener.
	 */
	@Override
	public void connectionRemoved(ConnectionInfo info) {
		log.debug("Repository detected Connection delete : ["+info.getId()+"]");
		Connection live = connections.get(info.getId());
		if (live != null) {
			// Remove Connection
			connections.remove(info.getId());
			log.debug("Repository found and deleted Connection ["+info.getId()+"]");
		}
		
		ConnectionAuthentication auth = connectionAuthentications.get(info.getId());
		if(auth != null) {
			// Remove Authentication Token
			connectionAuthentications.remove(info.getId());
			log.debug("Repository found and deleted ConnectionAuthentication for ["+info.getId()+"]");
		}
		
	}

}
