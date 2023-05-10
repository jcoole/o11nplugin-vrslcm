/**
 *
 */
package com.sprockitconsulting.vrslcm.plugin.endpoints;

import com.sprockitconsulting.vrslcm.plugin.scriptable.ConnectionInfo;

/**
 * This interface is pulled pretty much wholesale from the VMware documentation.
 * @author VMware SDK
 */
public interface IConfigurationChangeListener {
	/*
	 * Invoked when the ConnectionInfo input is updated
	 */
	void connectionUpdated(ConnectionInfo info);

	/*
	 * Invoked when the ConnectionInfo input is deleted
	 */
	void connectionRemoved(ConnectionInfo info);
}
