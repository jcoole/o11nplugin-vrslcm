package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
/**
 * Folder used in the Inventory view to hold Credentials.
 * @author justin
 */
@VsoObject(description = "Folder containing LCM Credentials in the Inventory Tab.", create = false)
@VsoFinder(
	name = "CredentialFolder", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Folder containing LCM Credentials in the Inventory Tab.", // shows up in the 'Types'
	idAccessor = "getId()", // method in the class to use for specific lookup
	image = "images/folder.png", // relative path to image in inventory use
	hidden = true
)
public class CredentialFolder extends Folder {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(CredentialFolder.class);
	
	/**
	 * @param connection The Connection ID associated to the folder.
	 */
	public CredentialFolder(String connectionId) {
		super();
		this.name = "Credentials";
		this.id = "Credentials@"+connectionId;
		log.debug("Constructor initialized successfully");
	}
}
