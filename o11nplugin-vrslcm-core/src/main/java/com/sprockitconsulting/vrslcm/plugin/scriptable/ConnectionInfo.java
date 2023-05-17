package com.sprockitconsulting.vrslcm.plugin.scriptable;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionPersister;
import com.vmware.o11n.plugin.sdk.annotation.VsoConstructor;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * @author justin
 * Represents the configuration for a vRSLCM Server connection.
 * Used by the vRO Platform EndpointConfigurationService to store values for future use.
 */
@VsoObject(description = "Represents the configuration for a vRSLCM Server connection. This object is constructed for Create/Update workflow calls.")
public class ConnectionInfo {
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ConnectionInfo.class);
	
	private String id;
	private String name;
	private String host;
	private String userName;
	private String userDomain;
	private String identityManagerHost;
	private String identityManagerClientId;
	
	// These two values will be encrypted via Orchestrator EndpointConfigurationService.
	private String userPassword;
	private String identityManagerClientSecret;
	
	@VsoConstructor(description="Default constructor of the vRSLCM Server Connection Info object. The 'id' field is default populated.")
	public ConnectionInfo() {
		log.debug("Calling default constructor");
		this.id = UUID.randomUUID().toString();
	}

	/** 
	 * This constructor is used during validation.
	 */
	public ConnectionInfo(String id) {
		this.id = id;
	}

	/**
	 * This constructor is used during the ConnectionPersister getConnectionInfo() call to make it immutable (hence the 'final').
	 */
	public ConnectionInfo(final ConnectionInfo config) {
		this.id = config.id;
		this.name = config.name;
		this.host = config.host;
		this.userName = config.userName;
		this.userPassword = config.userPassword;

	}

	@VsoProperty(description="The ID of the vRSLCM Server Connection inside of Orchestrator.", readOnly = true)
	public String getId() {
		return id;
	}
	@VsoProperty(description="The Friendly Name of the vRSLCM Server Connection.")
	public String getName() {
		return name;
	}
	@VsoProperty(description="The Hostname of the vRSLCM Server Connection.")
	public String getHost() {
		return host;
	}
	@VsoProperty(description="The User Account used in the vRSLCM Server Connection.")
	public String getUserName() {
		return userName;
	}
	@VsoProperty(description = "The User Domain. If unspecified, @local is assumed.")
	public String getUserDomain() {
		return userDomain;
	}

	@VsoProperty(description="The User Password used in the vRSLCM Server Connection. Encrypted by the platform.")
	public String getUserPassword() {
		return userPassword;
	}
	
	@VsoProperty(description="The Authentication Host of the vRSLCM Server Connection to VMware Identity Manager. "
			+ "If set, authentication requests are redirected here with a Client ID and Secret for authorization."
			+ "Users in the @local scope do not use this field for authentication.")
	public String getIdentityManagerHost() {
		return identityManagerHost;
	}
	
	@VsoProperty(description="The Required OAuth2.0 Client ID to use in authentication requests to VMware Identity Manager.")
	public String getIdentityManagerClientId() {
		return identityManagerClientId;
	}
	
	@VsoProperty(description="The Required OAuth2.0 Client Secret to use in authentication requests to VMware Identity Manager. Encrypted by the platform.")
	public String getIdentityManagerClientSecret() {
		return identityManagerClientSecret;
	}

	
	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserDomain(String userDomain) {
		this.userDomain = userDomain;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public void setIdentityManagerHost(String identityManagerHost) {
		this.identityManagerHost = identityManagerHost;
	}

	public void setIdentityManagerClientId(String identityManagerClientId) {
		this.identityManagerClientId = identityManagerClientId;
	}

	public void setIdentityManagerClientSecret(String identityManagerClientSecret) {
		this.identityManagerClientSecret = identityManagerClientSecret;
	}
}
