package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

@VsoObject(description = "CM Content", create = false)
@VsoFinder(
	name = "ContentManagementFolder", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "CM Content", // shows up in the 'Types'
	idAccessor = "getId()", // method in the class to use for specific lookup
	image = "images/cm32.png", // relative path to image in inventory use
	hidden = true // hides the type from API Explorer 'types' section.
)
public class ContentManagementFolder extends Folder {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ContentManagementFolder.class);
	
	/**
	 * @param connection The Connection ID associated to the folder.
	 */
	public ContentManagementFolder(String connectionId) {
		super();
		this.name = "Content Management";
		this.id = "ContentManagement@"+connectionId;
		log.debug("Constructor initialized successfully");
	}
}
