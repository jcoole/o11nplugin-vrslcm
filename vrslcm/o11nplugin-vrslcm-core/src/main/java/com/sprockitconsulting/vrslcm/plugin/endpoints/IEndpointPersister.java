/**
 *
 */
package com.sprockitconsulting.vrslcm.plugin.endpoints;

import java.util.List;

import com.sprockitconsulting.vrslcm.plugin.scriptable.ConnectionInfo;

/**
 * Interface based on the available public VMware docs and guides.
 * This interface is implemented by ConnectionPersister to manage Connections to/from Orchestrator's EndpointConfiguration service.
 * @author VMware SDK
 *
 */
public interface IEndpointPersister {
	/*
	 * Returns a collection of all stored configurations (resources under a folder with the plug-in name)
	 */
	public List<ConnectionInfo> findAll();

	/*
	 * Returns a collection by its ID or null if not found
	 */
	public ConnectionInfo findById(String id);

	/*
	 * Stores a connection info or updates it if already available.
	 * The persister checks the availability of a connection by its ID
	 */
	public ConnectionInfo save(ConnectionInfo connection);

	/*
	 * Deletes a connection info.
	 * The persister will use the ID of the connection
	 */
	public void delete(ConnectionInfo connectionInfo);

	/*
	 * Allows us to subscribe to the events of the persister.
	 * For example, if a connection is deleted, the persister will trigger an event, notifying all subscribers.
	 */
	void addChangeListener(IConfigurationChangeListener listener);

	/*
	 * Forces the persister to read all the configurations and trigger the events.
	 * This method is invoked when the plug-in is loaded on server start-up.
	 */
	public void load();
}
