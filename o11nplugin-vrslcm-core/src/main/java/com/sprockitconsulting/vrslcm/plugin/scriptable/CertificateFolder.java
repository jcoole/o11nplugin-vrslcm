package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.o11n.plugin.sdk.annotation.Cardinality;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;

/**
 * Folder used in the Inventory view to hold Certificates.
 * @author justin
 */
@VsoObject(description = "Folder containing Certificates in the Inventory Tab.", create = false)
@VsoFinder(
	name = "CertificateFolder", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Folder containing LCM Certificates in the Inventory Tab.", // shows up in the 'Types'
	idAccessor = "getId()", // method in the class to use for specific lookup
	image = "images/folder.png", // relative path to image in inventory use
	relations = {
		@VsoRelation(name = "Certificates", type = "Certificate", inventoryChildren = true, cardinality = Cardinality.TO_MANY)
	}
)
public class CertificateFolder extends Folder {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(CertificateFolder.class);

	public CertificateFolder(String connectionId) {
		super(); // call the Folder constructor
		this.name = "Certificates";
		this.id = "Certificates@"+connectionId;
		log.debug("Constructor initialized successfully");
	}

}
