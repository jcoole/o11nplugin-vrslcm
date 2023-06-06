package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * This class is a generic class used in creation of folders in the vRO Inventory.
 * @author justin
 *
 */
public abstract class Folder extends BaseLifecycleManagerObject {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(Folder.class);

	public String name; // This value will appear in the vRO Inventory view when the folder shows up.
	public String id; // This ID should be in the form of "Datacenters@[ConnectionId] as a string to preserve uniqueness when instantiated

	public Folder() {
		
	}

	@VsoProperty(description = "Name of the folder")
	public String getName() {
		return name;
	}

	@VsoProperty(description = "ID of the folder")
	public String getId() {
		return id;
	}


	public void setName(String name) {
		this.name = name;
	}

	public void setId(String id) {
		this.id = id;
	}


	@Override
	public String toString() {
		return "Folder [ type= "+this.getClass().getName()+", name= " + name + ", id=" + id+"]";
	}


}
