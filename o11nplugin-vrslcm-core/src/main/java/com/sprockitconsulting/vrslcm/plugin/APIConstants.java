package com.sprockitconsulting.vrslcm.plugin;

// This interface is to just hold global constants.
// They can be called simply via 'Constants.FINDER_PLUGIN' for example.
public interface APIConstants {

	public static final String FINDER_PLUGIN = "Plugin";

	/**
	 *  Lifecycle Operations Finders.
	 *  The 'abbrevated' values represent the respective folders in the inventory.
	 *  So, the DC would be the 'Datacenters' folder, which is expanded to the actual list.
	 */

	public static final String FINDER_PLUGIN_TO_CONNECTION = "Connections";
	public static final String FINDER_DC_TO_DATACENTER = "Datacenters";
	public static final String FINDER_ENV_TO_ENVIRONMENT = "Environments";
	public static final String FINDER_REQ_TO_REQUESTS = "Requests";
	public static final String FINDER_DATACENTER_TO_VIRTUALCENTER = "VirtualCenters";
	public static final String FINDER_ENVIRONMENT_TO_PRODUCT = "Products";
	public static final String FINDER_PRODUCT_TO_NODE = "ProductNodes";

	/** 
	 * Locker Finders.
	 * The 'abbrevated' values represent the respective folders in the inventory.
	 * So, the Cert would be the 'Certificates' folder, which is expanded to the actual list.
	 */
 
	public static final String FINDER_CERT_TO_CERTIFICATE = "Certificate";
	public static final String FINDER_PASS_TO_PASSWORD = "Password";
	public static final String FINDER_LIC_TO_LICENSE = "License";

	// Declares the Inventory Relations.
	// End Finders/Relations

	// Declares the Relative URI Templates along with API version(s) for the internal REST Client on supported objects.

	// Locker Endpoints
	public static final String URI_LOCKER_CERTIFICATES = "/lcm/locker/api/v2/certificates";
	public static final String URI_LOCKER_LICENSES = "/lcm/locker/api/v2/licenses/";
	public static final String URI_LOCKER_PASSWORDS = "/lcm/locker/api/v2/passwords/";
	public static final String URI_LOCKER_LICENSE_BY_ALIAS = "/lcm/locker/api/v2/licenses/alias/";
	
	// Lifecycle Operations (LCOPS) Endpoints
	public static final String URI_LCOPS_DATACENTERS = "/lcm/lcops/api/v2/datacenters/";
	public static final String URI_LCOPS_ENVIRONMENTS = "/lcm/lcops/api/v2/environments/";
	public static final String URI_LCOPS_REQUESTS = "/lcm/request/api/v2/requests/";
	public static final String URI_LCOPS_VIRTUALCENTERS_BY_DC = "/lcm/lcops/api/v2/datacenters/{dcId}/vcenters";
	public static final String URI_LCOPS_VIRTUALCENTERS_BY_DC_AND_VALUE = "/lcm/lcops/api/v2/datacenters/{dcId}/vcenters/{vc}";


	/*
	 * Some ASCII art on the layout of the Inventory.
	 *
	 * vRSLCM (ROOT)
	 * 		- MyLCMServer (CONNECTION)
	 * 			- Datacenters (folder)
	 * 				- Datacenter01 (Datacenter)
	 * 				- Datacenter02 (Datacenter)
	 * 			- Environments (folder)
	 * 				- globalenvironment (Environment)
	 * 				- MyEnvironment01 (Environment)
	 * 				- MyEnvironment02 (Environment)
	 * 			- Certificates (folder)
	 * 				- MyVRACert (Certificate)
	 * 				- MyVCCert (Certificate)
	 * 			- Passwords (folder)
	 * 				- MyPassword01 (Password)
	 * 			- Licenses (folder)
	 * 				- SomeLicense01 (License)
	 * 			- Requests (folder)
	 * 				- ArrayOfRequests (Request)
	 */
}
