/**
 *
 */
package com.sprockitconsulting.vrslcm.plugin.scriptable;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionPersister;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;
import com.vmware.o11n.plugin.sdk.spring.AbstractSpringPluginFactory;

import ch.dunes.vso.sdk.api.IPluginFactory;


/**
 * This class is used as a Scriptable Object in Orchestrator to perform CRUD operations as a front end to ConnectionPersister.
 * @see String#toLowerCase() ConnectionPersister
 * @author justin
 */

@VsoObject(description = "Management of vRSLCM Connections.", singleton = true)
public class ConnectionManager {

	@Autowired
	private ConnectionPersister persister;

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ConnectionManager.class);

	public ConnectionManager() {

	}

	// makes the type *directly* referenceable in scripts
    public static ConnectionManager createScriptingSingleton(IPluginFactory factory) {
        return ((AbstractSpringPluginFactory) factory).createScriptingObject(ConnectionManager.class);
    }

	@VsoMethod(description = "Retrieves an array of all vRSLCM Connections in the Orchestrator inventory.")
	public ArrayList<Connection> getAllConnections() {
		log.debug("ConnectionManager is calling getAllConnections()");
		ArrayList<Connection> conns = new ArrayList<>();
		List<ConnectionInfo> configs = persister.findAll();
		// Construct array of objects.
		for (ConnectionInfo config : configs) {
			conns.add(new Connection(config));
		}
		log.debug("ConnectionManager found ["+conns.size()+"] entries in EndpointConfigurationService");
		return conns;
	}

	@VsoMethod(description = "Retrieves a vRSLCM Connection in the Orchestrator inventory with the given ID.")
	public Connection getConnectionById(@VsoParam(description = "ID of the vRSLCM Connection to retrieve.")String id)
	{
		log.debug("ConnectionManager is calling getConnectionById("+id+")");
		ConnectionInfo info = persister.findById(id);
		log.debug("ConnectionManager found ConnectionInfo ["+info.toString()+"]");
		return new Connection(info);
	}

	@VsoMethod(description = "Creates a new vRSLCM Connection in the Orchestrator inventory with the given information."
			+ "Setup your info with this : var info = new vRSLCMConnectionInfo()"
			+ "Then populate the necessary values before calling this method.")
	public Connection createConnection(
		@VsoParam(description = "vRSLCM Server ConnectionInfo object to use in creation. Construct this object in your script before calling the method.")ConnectionInfo info)
	{
		return new Connection(persister.save(info));
	}

	@VsoMethod(description = "Deletes a vRSLCM Connection from the Orchestrator inventory by the given ID.")
	public void deleteConnection(
		@VsoParam(description = "vRSLCM Server Connection to delete.")String id)
	{
		persister.delete(persister.findById(id));
	}
}
