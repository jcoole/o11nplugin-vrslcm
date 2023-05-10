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

import com.sprockitconsulting.vrslcm.plugin.component.ObjectFactory;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.ConnectionInfo;

/**
 * Connection Repository is used as a local cache of the plugin for live connections.
 * It implements the ConfigurationChangeListener so that any changes made to configurations are passed through here.
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
	 * ApplicationContext is injected here so that the Repository can create Connection Beans for other classes to use.
	 */
	@Autowired
	private ApplicationContext context;
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ConnectionRepository.class);
	
	/**
	 * Local cache of connections.
	 * 	String is the ConnectionInfo ID
	 *  Connection is the object.
	 */
	private final Map<String, Connection> connections;
	private final Map<String, ObjectFactory> objectFactories;
	
	
	public ConnectionRepository() {
		connections = new ConcurrentHashMap<>();
		objectFactories = new ConcurrentHashMap<>();
		log.debug("Constructor initialized");
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
	 * Get Factory by Connection ID
	 */
	public ObjectFactory findObjectFactory(String id) {
		return objectFactories.get(id);
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
	 * 		- Then, tell the ConnectionPersister to load all Connections from the Orchestrator DB
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
	 * Creates a factory with the connectionID
	 */
	private ObjectFactory createObjectFactory(Connection connection) {
		ObjectFactory ofb = (ObjectFactory) context.getBean("objectFactory", connection);
		log.debug("ObjectFactory created for Connection ID ["+connection.getId()+"]");
		return ofb;
	}
	
	
	/**
	 * Interface method from ConfigurationChangeListener
	 * 
	 * Called whenever a new vRSLCM Connection is added or updated.
	 * 
	 * In particular, this method will place the newly updated Connection into the 'connections' map, which can be accessed by other classes elsewhere.
	 * 
	 * @param info The ConnectionInfo object sent by the ConfigurationChangeListener.
	 */
	@Override
	public void connectionUpdated(ConnectionInfo info) {
		log.debug("Repository detected Connection update : ["+info.toString()+"]");
		Connection live = connections.get(info.getId());

		if (live != null) {
			// The connection exists in the repository, just update it.
			live.update(info);
		} else {
			// Connection did not previously exist, so create it and add to the repository.
			live = createConnection(info);
			connections.put(info.getId(), live);
		}
		
		// Now handle the factories
		ObjectFactory factory = objectFactories.get(info.getId());
		if(factory != null) {
			// factor is there, replace the connection
			factory.setConnection(live);
		} else {
			// factory not there, add it
			factory = createObjectFactory(live);
			objectFactories.put(info.getId(), factory);

		}
		
	}

	/**
	 * Interface method from ConfigurationChangeListener
	 * 
	 * Called whenever a vRSLCM Connection is removed.
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
	}

}
