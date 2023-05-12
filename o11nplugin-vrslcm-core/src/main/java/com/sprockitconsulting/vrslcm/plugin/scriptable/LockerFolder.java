package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.sdk.annotation.Cardinality;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoInventoryTab;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
/**
 * Represents the Locker content in the Inventory view.
 * @author justin
 */
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;
@VsoObject(description = "Locker Content", create = false)
@VsoFinder(
	name = "LockerFolder", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Locker Content", // shows up in the 'Types'
	idAccessor = "getId()", // method in the class to use for specific lookup
	image = "images/locker32.png", // relative path to image in inventory use
	hidden = true,
	// creating inventory tabs for creds/certs to see what that looks like
	inventoryTabs = {
		@VsoInventoryTab(displayName = "CertificatesInventoryTab", finderType = "Certificate", followSelection = true, relationName = "Certificates", type = "Certificate")
	},
	relations = {
		@VsoRelation(name = "CertificateFolders", type = "CertificateFolder", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "CredentialFolders", type = "CredentialFolder", inventoryChildren = true, cardinality = Cardinality.TO_ONE)
	}
	
)
public class LockerFolder extends Folder {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(LockerFolder.class);
	
	/**
	 * @param connection The Connection ID associated to the folder.
	 */
	public LockerFolder(String connectionId) {
		super();
		this.name = "Locker";
		this.id = this.name.trim()+"@"+connectionId;
		log.debug("Constructor initialized successfully");
	}
}
