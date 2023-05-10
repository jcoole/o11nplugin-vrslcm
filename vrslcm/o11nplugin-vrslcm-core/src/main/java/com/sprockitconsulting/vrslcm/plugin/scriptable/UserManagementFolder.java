package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

@VsoObject(description = "User Management Content", create = false)
@VsoFinder(
	name = "UserManagementFolder", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "User Management Content", // shows up in the 'Types'
	idAccessor = "getId()", // method in the class to use for specific lookup
	image = "images/um32.png", // relative path to image in inventory use
	hidden = true
)
public class UserManagementFolder extends Folder {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(UserManagementFolder.class);
	
	/**
	 * @param connection The Connection ID associated to the folder.
	 */
	public UserManagementFolder(String connectionId) {
		super();
		this.name = "User Management";
		this.id = this.name.trim()+"@"+connectionId;
		log.debug("Constructor initialized successfully");
	}
}
