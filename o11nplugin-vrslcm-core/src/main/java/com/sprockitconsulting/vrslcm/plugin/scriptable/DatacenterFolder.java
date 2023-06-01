package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.sdk.annotation.Cardinality;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;

/**
 * Folder used in the Inventory view to hold Datacenters.
 * @author justin
 */
@VsoObject(description = "Folder containing vRSLCM Datacenters in the Inventory Tab.", create = false)
@VsoFinder(
	name = "DatacenterFolder", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Folder containing vRSLCM Datacenters in the Inventory Tab.", // shows up in the 'Types'
	idAccessor = "getId()", // method in the class to use for specific lookup
	image = "images/folder.png", // relative path to image in inventory use
	hidden = true,
	relations = {
		@VsoRelation(name = "Datacenters", type = "Datacenter", inventoryChildren = true, cardinality = Cardinality.TO_MANY)
	}
)
public class DatacenterFolder extends Folder {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(DatacenterFolder.class);

	public DatacenterFolder(String connectionId) {
		super(); // call the Folder constructor
		this.name = "Datacenters";
		this.id = "Datacenters@"+connectionId;
		log.debug("Constructor initialized successfully");
	}
}
