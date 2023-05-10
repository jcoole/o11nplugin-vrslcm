package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;

/**
 * Represents an Error during Inventory enumeration.
 * This can be used to return error messages to the end user (no permission, etc) when browing the plugin inventory.
 * @author justin
 */

@VsoObject(description = "In case of an error when browsing inventory, this object will be presented.")
//Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
@VsoFinder(
	name = "InventoryError", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "A vRSLCM Inventory error.", // shows up in the 'Types'
	idAccessor = "getId()", // method in the class to use for specific lookup
	image = "images/error.png", // relative path to image in inventory use
	hidden = true // setting this means that the object will not show up in 'Types'
)
public class InventoryError {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(InventoryError.class);
	
	private String errorCode;
	private String errorDescription;
	private String errorName;
	
	public InventoryError() {
		log.debug("Constructor initialized");
	}
	public String getErrorCode() {
		return errorCode;
	}
	public String getErrorDescription() {
		return errorDescription;
	}
	public String getErrorName() {
		return errorName;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}
	public void setErrorName(String errorName) {
		this.errorName = errorName;
	}
	
	@Override
	public String toString() {
		return String.format("InventoryError [errorCode=%s, errorDescription=%s, errorName=%s]", errorCode,
				errorDescription, errorName);
	}
	
	
}
