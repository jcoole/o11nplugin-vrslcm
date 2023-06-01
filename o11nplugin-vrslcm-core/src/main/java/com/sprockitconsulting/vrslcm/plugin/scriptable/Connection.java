package com.sprockitconsulting.vrslcm.plugin.scriptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionAuthentication;
import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionRepository;
import com.sprockitconsulting.vrslcm.plugin.services.ConfigurationService;
import com.sprockitconsulting.vrslcm.plugin.services.LifecycleOperationsService;
import com.sprockitconsulting.vrslcm.plugin.services.LockerService;
//import com.sprockitconsulting.vrslcm.plugin.services.
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * Represents a vRSLCM Connection object in Orchestrator.
 * This is the basis for communication to the external server and child inventory objects.
 * @author justin
 */
@Component
@Qualifier(value = "connection")
@Scope(value = "prototype") // This indicates to Spring that a new instance should be returned each time it is called. This also means the bean will not be created at startup.
@VsoObject(description = "Represents a vRSLCM Server Connection.")
// Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
@VsoFinder(
	name = "Connection", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "A vRSLCM server connection in the inventory.", // shows up in the 'Types'
	idAccessor = "getId()", // method in the class to use for specific lookup
	image = "images/connection.png" // relative path to image in inventory use
)
public class Connection {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(Connection.class);
	
	@Autowired
	private ConnectionRepository repository;
	@Autowired
	private ApplicationContext context;
	
	private String name;
	private String id;
	private String host;
	private String userName;
	private String userDomain;
	private String identityManagerHost;
	private String identityManagerClientId;
	private ConnectionInfo connectionInfo;

	
	/**
	 * Note there is no default constructor. It is initialized only with a ConnectionInfo.
	 * Important to note as this is scoped as prototype.
	 * @see String#toLowerCase() init
	 */
	public Connection(ConnectionInfo info) {
		init(info);
	}

	@VsoProperty(description = "Display Name of the vRSLCM Connection in Orchestrator.")
	public String getName() {
		return name;
	}

	@VsoProperty(description = "Unique ID of the vRSLCM Connection in Orchestrator.")
	public String getId() {
		return id;
	}

	@VsoProperty(description="The Hostname of the vRSLCM Server.")
	public String getHost() {
		return host;
	}

	@VsoProperty(description="The User Account Name used in the vRSLCM Server Connection.")
	public String getUserName() {
		return userName;
	}
	
	@VsoProperty(description="The User Account Domain used in the vRSLCM Server Connection. If unspecified, @local is assumed.")
	public String getUserDomain() {
		return userDomain;
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
	
	
	public ConnectionAuthentication getConnectionAuthenticationFromRepository() {
		log.debug("Returning ConnectionAuthentication with ID ["+this.id+"]");
		return repository.findConnectionAuthentication(this.id);
	}


	/**
	 * Initializes a new Connection from the ConnectionInfo, and populates the fields.
	 */
	private void init(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
		this.name = connectionInfo.getName();
		this.id = connectionInfo.getId();
		this.host = connectionInfo.getHost();
		this.userName = connectionInfo.getUserName();
		this.userDomain = connectionInfo.getUserDomain();
		this.identityManagerHost = connectionInfo.getIdentityManagerHost();
		this.identityManagerClientId = connectionInfo.getIdentityManagerClientId();

		log.debug("Initializing new Connection with info ["+connectionInfo.toString()+"]");
	}
	

	/**
	 * Updates this connection with the provided info.
	 * Technically, it recreates it from scratch.
	 */
	public synchronized void update(ConnectionInfo connectionInfo) {
		if (this.connectionInfo != null && !connectionInfo.getId().equals(this.connectionInfo.getId())) {
			throw new IllegalArgumentException("You cannot update the ID of a Connection!");
		}
		init(connectionInfo);
	}

	/**
	 * Retrieve the LifecycleOperationsService object, enabling the Connection to perform operations on those scoped objects.
	 */
	@VsoMethod(description = "Use this service to perform operations on Lifecycle Operations scoped objects.")
	public LifecycleOperationsService getLifecycleOperationsService() {
		log.debug("Setting up Lifecycle Operations Service with connection ["+this.getId()+"]");
		// Using the Spring context, get an prototype bean of the LCOPS service, initialized with the connection ID.
		// This will allow it to get authentication, etc from the Repository
		LifecycleOperationsService svc = (LifecycleOperationsService) context.getBean("lifecycleOperationsService", repository.findLiveConnection(this.getId()) );
		return svc;
	}

	/**
	 * Retrieve the LockerService object, enabling the Connection to perform operations on those scoped objects.
	 */
	@VsoMethod(description = "Use this service to perform operations on Locker scoped objects.")
	public LockerService getLockerService() {
		log.debug("Setting up Locker Service with connection ["+this.getId()+"]");
		LockerService svc = (LockerService) context.getBean("lockerService", repository.findLiveConnection(this.getId()));
		return svc;
	}
	
	/**
	 * Retrieve the ConfigurationService object, enabling the Connection to perform operations on the LCM Server itself.
	 */
	@VsoMethod(description = "Use this service to perform operations on the LCM Server itself.")
	public ConfigurationService getConfigurationService() {
		log.debug("Setting up Configuration Service with connection ["+this.getId()+"]");
		ConfigurationService svc = (ConfigurationService) context.getBean("configurationService", repository.findLiveConnection(this.getId()));
		return svc;
	}
/*
	public LockerService getLockerService() {
		log.debug("Setting up Locker with connection ["+this.getId()+"]");
		LockerService svc = new LockerService();
		log.debug("LockerService connection: "+repository.findLiveConnection(this.getId()) );
		svc.setConnection(repository.findLiveConnection(this.getId()));
		log.debug("LockerService factory: "+repository.findObjectFactory(this.getId()) );
		svc.setObjectFactory(repository.findObjectFactory(this.getId()));
		log.debug(svc.toString());
		return svc;
	}
*/
	
	public synchronized ConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}

	@Override
	public String toString() {
		return String.format(
				"Connection [name=%s, id=%s, host=%s, userName=%s, userDomain=%s, identityManagerHost=%s, identityManagerClientId=%s]",
				name, id, host, userName, userDomain, identityManagerHost, identityManagerClientId);
	}




}
