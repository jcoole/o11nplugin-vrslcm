package com.sprockitconsulting.vrslcm.plugin.scriptable;

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
	
	private String reference;
	private String type;
	private String resourceId;
	private String alias;
	
	public LockerReference() {}
	
	public LockerReference(String ref) {
		// First value must be 'locker' - check for this otherwise it's invalid
		if(ref.split(":")[0].equals("locker") ) {
			this.reference = ref;
			this.type = ref.split(":")[1];
			this.resourceId = ref.split(":")[2];
			this.alias = ref.split(":")[3];
		} else {
			throw new RuntimeException("The Locker Reference value of ["+ref+"] is invalid, it must follow the format of [locker:<type>:<id>:<alias>]");
			
		}
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
	
	@Override
	public String toString() {
		return String.format("LockerReference [reference=%s, type=%s, resourceId=%s, alias=%s, connection=%s]",
				reference, type, resourceId, alias, connection);
	}
	
}
