package com.sprockitconsulting.vrslcm.plugin.scriptable;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoConstructor;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoGauge;
import com.vmware.o11n.plugin.sdk.annotation.VsoInventoryTab;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoParam;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;
import com.vmware.o11n.plugin.sdk.annotation.VsoTrigger;
import com.vmware.o11n.plugin.sdk.annotation.VsoTriggerProperty;

@VsoObject(description = "Represents a Credential (Password) in LCM.")
@VsoFinder(
	name = "Credential", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "Represents a Credential (Password) in LCM.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/password.png" // relative path to image in inventory use
)
public class Credential extends BaseLifecycleManagerObject {

	private String name;
	private String alias;
	private String userName;
	private String password;
	private String passwordDescription;
	private String tenant;
	private String createdOn;
	private String lastUpdatedOn;
	private boolean isReferenced;
	
	@VsoConstructor(description = "Empty constructor - set your values in your script before calling for creation.")
	public Credential() {
		super();
	}
	
	@VsoConstructor(description = "Constructor containing all required fields for creation.")
	public Credential(String alias, String user, String password, String description) {
		super();
		this.alias = alias;
		this.name = alias;
		this.userName = user;
		this.password = password;
		this.passwordDescription = description;
	}

	@VsoProperty(description = "Alias of the Credential")
	public String getAlias() {
		return alias;
	}
	@VsoProperty
	public String getUserName() {
		return userName;
	}
	@VsoProperty 
	public String getPassword() {
		return password;
	}
	@VsoProperty
	public String getPasswordDescription() {
		return passwordDescription;
	}
	@VsoProperty
	public String getName() {
		return name;
	}
	@VsoProperty
	public String getTenant() {
		return tenant;
	}
	@VsoProperty
	public String getCreatedOn() {
		return createdOn;
	}
	@VsoProperty
	public String getLastUpdatedOn() {
		return lastUpdatedOn;
	}
	@VsoProperty
	public boolean isReferenced() {
		return isReferenced;
	}

	@Override
	@JsonProperty("vmid")
	public void setResourceId(String id) {
		super.setResourceId(id);
	}
	@JsonProperty("alias")
	public void setAlias(String alias) {
		this.alias = alias;
		this.name = alias;
	}
	@JsonProperty("userName")
	public void setUserName(String userName) {
		this.userName = userName;
	}
	@JsonProperty("password")
	public void setPassword(String password) {
		this.password = password;
	}
	@JsonProperty("passwordDescription")
	public void setPasswordDescription(String passwordDescription) {
		this.passwordDescription = passwordDescription;
	}

	public void setName(String name) {
		this.name = name;
	}
	@JsonProperty("tenant")
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}
	@JsonProperty("createdOn")
	public void setCreatedOn(long createdOn) {
		this.createdOn = Instant.ofEpochMilli(createdOn).toString();
	}
	@JsonProperty("lastUpdatedOn")
	public void setLastUpdatedOn(long lastUpdatedOn) {
		this.lastUpdatedOn = Instant.ofEpochMilli(lastUpdatedOn).toString();
	}
	@JsonProperty("referenced")
	public void setReferenced(boolean isReferenced) {
		this.isReferenced = isReferenced;
	}

	

	

}
