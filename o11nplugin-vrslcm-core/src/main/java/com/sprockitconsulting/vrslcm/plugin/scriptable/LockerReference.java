package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.springframework.beans.factory.annotation.Autowired;

import com.sprockitconsulting.vrslcm.plugin.services.CertificateService;
import com.sprockitconsulting.vrslcm.plugin.services.CredentialService;

/**
 * Locker references are string values to link between entities.
 * 
 * Example: locker:[certificate|password]:[UUID]:[Alias]
 * 	- Locker Object type
 * 	- Reference ID
 * 	- Alias
 * 
 * @author justin
 *
 */
public class LockerReference extends BaseLifecycleManagerObject {

	@Autowired
	private CertificateService certificateService;
	@Autowired
	private CredentialService credentialService;
	
	private String reference;
	private String type;
	private String resourceId;
	private String alias;
	
	public LockerReference() {
		
	}
	
	public LockerReference(String ref) {
		this.reference = ref;
		this.type = ref.split(":")[1];
		this.resourceId = ref.split(":")[2];
		this.alias = ref.split(":")[3];
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public <T extends BaseLifecycleManagerObject> T getReferencedLockerResource() {
		T resource = null;
		switch(type) {
		case "certificate":
			resource = (T) certificateService.getCertificateByValue(connection, resourceId);
			break;
		case "password":
			resource = (T) credentialService.getByValue(connection, resourceId);
			break;
		}
		return resource;
	}

}
