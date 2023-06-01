package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.sdk.annotation.Cardinality;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;

/**
 * Represents an Environment folder in the Inventory view.
 * @author justin
 */
@VsoObject(description = "Folder containing vRSLCM Environments in the Inventory Tab.", create = false)
@VsoFinder(
	name = "EnvironmentFolder", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Folder containing vRSLCM Environments in the Inventory Tab.", // shows up in the 'Types'
	idAccessor = "getId()", // method in the class to use for specific lookup
	image = "images/folder.png", // relative path to image in inventory use
	hidden = true,
	relations = {
		@VsoRelation(name = "Environments", type = "Environment", inventoryChildren = true, cardinality = Cardinality.TO_MANY)
	}
)
public class EnvironmentFolder extends Folder {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(EnvironmentFolder.class);

	public EnvironmentFolder(String connectionId) {
		super();
		this.name = "Environments";
		this.id = "Environments@"+connectionId;
		log.debug("Constructor initialized successfully");
	}

}
