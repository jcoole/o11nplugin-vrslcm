package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

/**
 * Represents the Lifecycle Operations content in the Inventory view.
 * @author justin
 */
@VsoObject(description = "Lifecycle Operations Content", create = false)
@VsoFinder(
	name = "LifecycleOperationsFolder", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Lifecycle Operations Content", // shows up in the 'Types'
	idAccessor = "getId()", // method in the class to use for specific lookup
	hidden = true, // hides from the 'types' section
	image = "images/lcops32.png" // relative path to image in inventory use
)
public class LifecycleOperationsFolder extends Folder {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(LifecycleOperationsFolder.class);
	
	/**
	 * @param connection The Connection ID associated to the folder.
	 */
	public LifecycleOperationsFolder(String connectionId) {
		super();
		this.name = "Lifecycle Operations";
		this.id = "LifecycleOperations@"+connectionId;
		log.debug("Constructor initialized successfully");
	}
}
