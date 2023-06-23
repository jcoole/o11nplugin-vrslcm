package com.sprockitconsulting.vrslcm.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionRepository;
import com.sprockitconsulting.vrslcm.plugin.products.BaseProduct;
import com.sprockitconsulting.vrslcm.plugin.scriptable.CertificateFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.ContentManagementFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.CredentialFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.DatacenterFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
import com.sprockitconsulting.vrslcm.plugin.scriptable.EnvironmentFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Folder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.LifecycleOperationsFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.LockerFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.RequestFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.UserManagementFolder;
import com.sprockitconsulting.vrslcm.plugin.services.*;
import com.vmware.o11n.plugin.sdk.spring.AbstractSpringPluginFactory;
import com.vmware.o11n.plugin.sdk.spring.InventoryRef;

import ch.dunes.vso.sdk.api.QueryResult;

/**
 * This class is responsible for the core lookup logic of all Findable types in the Inventory.
 * The key methods used are:
 * 		* find() - called anytime a VsoFinder annotated class needs to be serialized
 * 		* findRootChildrenInRelation() - called when the Inventory 'root' finder is invoked
 * 		* findChildrenInRelation() - called anytime a VsoFinder annotated class has a relation, so as to return a list of objects.
 * 
 * @author justin
 *
 */
public final class vRSLCMPluginFactory extends AbstractSpringPluginFactory {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(vRSLCMPluginFactory.class);

	@Autowired
	private ConnectionRepository repository;
	@Autowired
	private DatacenterService datacenterService;
	@Autowired
	private EnvironmentService environmentService;
	@Autowired
	private RequestService requestService;
	@Autowired
	private CredentialService credentialService;
	@Autowired
	private CertificateService certificateService;
	@Autowired
	private VirtualCenterService virtualCenterService;
	
	/**
	 * This method is called when the Finder is invoked to serialize the object for the specified type.
	 * You can reproduce this by expanding a variable of the type to show the properties in a Orchestrator variable.
	 * In this plugin for remote objects, the Id inside of InventoryRef follows this format:
	 * 		[Resource ID]@[Connection ID]
	 * This structure is to support lookups based on server, type, and value.
	 */
    @Override
    public Object find(InventoryRef ref) {
      	log.debug("Factory : calling FIND on type ["+ref.getType()+"] with id ["+ref.getId()+"]");

      	if(ref.isOfType("Connection")) {
    		// A Connection is tied to the vRO EndpointConfigurationService and ConnectionRepository, and requires no API calls outbound.		
    		Connection connection = repository.findLiveConnection(ref.getId());
			log.debug("Factory found Connection ["+connection.getId()+"] in repository");
    		return connection;
        } else {
    		// For these find requests, you will extract the Connection ID and Resource ID passed in.
        	String resourceId = ref.getId().split("@")[0]; // Should be the resource ID.
        	String connectionId = ref.getId().split("@")[1]; // Should be connection
        	
        	// Now that you know the connectionId, you can get the Connection of the associated resource.
        	Connection connection = repository.findLiveConnection(connectionId);
        	
        	// Now, based on the InventoryRef 'type' - run the appropriate service method to get what you need.
	    	switch(ref.getType()) {
	    	case "Connection":
	    		return connection; // TODO: remove the if/else logic above to consolidate here
	    	case "Datacenter":
	    		return datacenterService.getByValue(connection, resourceId);
	    	case "Environment":
	    		return environmentService.getByValue(connection, resourceId);
	    	case "Request":
	    		return requestService.getByValue(connection, resourceId);
	    	case "Certificate":
	    		return certificateService.getCertificateByValue(connection, resourceId);
	    	case "Credential":
	    		return credentialService.getByValue(connection, resourceId);
	    	case "AutomationProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "vra");
	    	case "IdentityManagerProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "vidm");
	    	case "LogInsightProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "vrli");
	    	case "NetworkInsightProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "vrni");
	    	case "OperationsProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "vrops");
	    	case "NetworkInsightProxyProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "vrnicloudproxy");
	    	case "OperationsProxyProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "vropscloudproxy");
	    	case "OrchestratorProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "vro");
	    	case "SaltStackProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "vssc");
	    	case "BusinessCloudProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "vrb");
	    	case "CloudProxyProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "cloudproxy");
	    	case "ExtensibilityProxyProduct":
	    		return environmentService.getSpecificProduct(connection, resourceId, "abxcloudproxy");
	    	case "ProductNode":
	    		// Product Nodes and their values are several depths down in the JSON, so to support lookups, split by the ":"
	    		// Resource ID is - [vmName]:[type]:[product]:[environmentId]
	    		String vmName = resourceId.split(":")[0];
	    		String type = resourceId.split(":")[1];
	    		String productId = resourceId.split(":")[2];
	    		String environmentId = resourceId.split(":")[3];
	    		return environmentService.getSpecificProductNode(connection, environmentId, productId, type, vmName);
	    	case "VirtualCenter":
	    		// VirtualCenter - <vcenter.fqdn.local>@<connection>
	    		return virtualCenterService.getByName(connection, resourceId);
	    	default:
	    		log.warn("Unknown finder case reached: "+ref.getType()+":"+ref.getId());
	    	}
        }
        return null;
    }

    /**
     * Looks up all of the resources based on the specified type and optional query.
     * This is used in workflow fields leveraging an input that is set to "Value Picker"
     * To support looking up resources in all Connections, all registered Connections are looped through and services called.
     */
    @Override
    public QueryResult findAll(String type, String query) {
    	log.debug("findAll() begin with type ["+type+"] and query ["+query+"]");
    	QueryResult results = new QueryResult();
    	for (Connection connection : repository.findAll() ) {
			log.debug("findAll() searching for "+type+" in Connection ["+connection.getName()+":"+connection.getId()+"]");
			switch(type) {
    		case "Connection":
    			results.addElement(connection);
    			break;
    		case "Datacenter":
   				results.addElements(datacenterService.getAll(connection));
    			break;
    		case "Environment":
    			results.addElements(environmentService.getAll(connection));
    			break;
    		case "Credential":
    			results.addElements(credentialService.getAll(connection));
    			break;
    		case "Certificate":
    			results.addElements(certificateService.getAllCertificates(connection));
    			break;
    		case "VirtualCenter":
    			results.addElements(virtualCenterService.getAll(connection));
    			break;
    		default:
    			log.warn("findAll() could not handle a search for the type ["+type+"] in the factory, a case should be added for it");
			}
		}

    	log.debug("findAll() - QueryResult object "+type+" :: Count ["+results.getElements().size()+"]");
    	return results;
    }

    /**
     * This method is called in the Orchestrator Inventory screen when enumerating the 'initial' children from the Plugin.
     * In the vRSLCM Plugin case, this would be a Connection object.
     */
    @Override
    public List<?> findChildrenInRootRelation(String type, String relationName) {
    	// ConnectionRepository has this information populated at startup as a Collection, so retrieve it and convert to a list for return.
    	return repository.findAll().stream().collect(Collectors.toList());
    }
    
    /**
     * This method is called for any Inventory items configured to have relationships in the tree.
     * These are created in the class as a VsoFinder (thus creating a Findable Type)
     * The relationships are managed in the vRSLCMModuleBuilder class, or @VsoRelation annotations in the parent class.
     */
	@Override
    public List<?> findChildrenInRelation(InventoryRef parent, String relationName) {
    	log.debug("findChildrenInRelation - Starting - InventoryRef ["+parent.getType()+"] ["+parent.getId()+"] Relation Name ["+relationName+"]");
    	
    	// All child items have an association to a particular Connection.
    	// The inventoryRef ID value is structured so that the format is [ResourceID]@[ConnectionID].
     	String connectionId;
        if(parent.getId().indexOf("@") != -1) {
        	// @ is present, split it.
        	connectionId = parent.getId().split("@")[1];
        } else {
        	// not present
        	connectionId = parent.getId();
        }
        
        // Retrieve the connection by ID in the repository, which can then be passed to service classes and retrieve the correct info.
        Connection connection = repository.findLiveConnection(connectionId);

    	
		// Connection -> Service Folders
    	// Folders are static, so the constructor just needs any ID to be built - so use the Connection ID.
        if(parent.isOfType("Connection")) {
    		log.debug("findChildrenInRelation - Creating service level folders for Connection ID ["+connectionId+"]");
    		ArrayList<Folder> serviceFolders = new ArrayList<Folder>();
    		switch(relationName) {
    		case "LifecycleOperationsFolders":
    			serviceFolders.add(new LifecycleOperationsFolder(connectionId) );
    			break;
    		case "LockerFolders":
       			serviceFolders.add(new LockerFolder(connectionId) );
    			break;
    		case "UserManagementFolders":
    			serviceFolders.add(new UserManagementFolder(connectionId) );
    			break;
    		case "ContentManagementFolders":
    			serviceFolders.add(new ContentManagementFolder(connectionId) );
    			break;
    		default:
    			log.warn("Default folder: "+relationName+" - add handler for this value!");
    			break;
    		}

    		return serviceFolders;
	 		
    	} else if(parent.isOfType("LifecycleOperationsFolder")) {
    		// These folders are the items managed in the LifecycleOperations section of the platform.
    		log.debug("findChildrenInRelation - Creating LifecycleOperations level folders for Connection ID ["+connectionId+"]");
    		ArrayList<Folder> lifecycleOperationsFolders = new ArrayList<Folder>();

	 		try {
	    		if(relationName.equals("DatacenterFolders")) {
	    			lifecycleOperationsFolders.add(new DatacenterFolder(connectionId) );
	    		} else if(relationName.equals("EnvironmentFolders") ) {
	    			lifecycleOperationsFolders.add(new EnvironmentFolder(connectionId) );
	    		} else if(relationName.equals("RequestFolders") ) {
	    			lifecycleOperationsFolders.add(new RequestFolder(connectionId) );
	    		} 
	 		} catch (Exception e) {
	 			log.error("Unable to construct first level LCOPS folders with Connection ID ["+connectionId+"] - Exception was "+e.toString());
	 		}
	 		return lifecycleOperationsFolders;
    	} else if(parent.isOfType("LockerFolder")) {
    		// These folders are the items managed in the Locker section of the platform.
    		log.debug("findChildrenInRelation - Creating Locker level folders for Connection ID ["+connectionId+"]");
    		ArrayList<Folder> lockerFolders = new ArrayList<Folder>();
	 		
    		try {
	    		if(relationName.equals("CertificateFolders")) {
	    			lockerFolders.add(new CertificateFolder(connectionId) );
	    		} else if(relationName.equals("CredentialFolders")) {
	    			lockerFolders.add(new CredentialFolder(connectionId) );
	    		} 
	 		} catch (Exception e) {
	 			log.error("Unable to construct first level Locker folders with Connection ID ["+connectionId+"] - Exception was "+e.toString());
	 		}
	 		return lockerFolders;
    	} else if(parent.isOfType("ContentManagementFolder")) {	
    		// TODO: Implement
    		return null;

    	} else if(parent.isOfType("UserManagementFolder")) {
    		// TODO: Implement
    		return null;
    	} else {
    		// These are objects that require an API connection be established and the results returned.
    		// To ensure multiple Connections are supported, each Object is associated with the ConnectionID value.
      		// Parent Type: DatacenterFolder -> Relation: Datacenters 
        	if(parent.isOfType("DatacenterFolder") && relationName.equals("Datacenters")) {
        		log.debug("findChildrenInRelation - Creating DATACENTERS for Connection ID ["+connectionId+"]");    			
        		try {
        			return datacenterService.getAll(connection);
        		} catch (RuntimeException e) {
        			log.error("Unable to retrieve Datacenters from the API service! Error was: "+e.getMessage());
        			e.printStackTrace();
				}
        	} 
        	// Parent type: Datacenter -> Relation: VirtualCenters
        	if(parent.isOfType("Datacenter") && relationName.equals("VirtualCenters")) {
        		String dcId = parent.getId().split("@")[0]; // vCenters do not have unique IDs, just associations with Datacenters.
        		log.debug("findChildrenInRelation - Creating VIRTUALCENTERS for Connection ID ["+connectionId+"] in Datacenter ["+dcId+"]");    			
        		try {
        			return datacenterService.getByValue(connection, dcId).getVirtualCenters();
        			//return datacenterService.findAllVirtualCentersInDatacenter(connection, dcId);
        		} catch (Exception e) {
        			log.error("Unable to retrieve vCenters associated to Datacenter ["+dcId+"] from the API service! Error was: "+e.getMessage());
        			e.printStackTrace();
				}
        	}
        	
        	// Parent Type: EnvironmentFolder -> Relation: Environments 
        	if(parent.isOfType("EnvironmentFolder") && relationName.equals("Environments")) {
        		log.debug("findChildrenInRelation - Creating ENVIRONMENTS for Connection ID ["+connectionId+"]"); 
        		try {
            		return environmentService.getAll(connection);
        		} catch (RuntimeException e) {
        			log.error("Unable to retrieve Environments from the API service! Error was: "+e.getMessage());
        			e.printStackTrace();
				}
        	}

        	// Parent Type: RequestFolder -> Requests
        	if(parent.isOfType("RequestFolder") && relationName.equals("Requests")) {
        		log.debug("findChildrenInRelation - Creating REQUESTS for Connection ID ["+connectionId+"]"); 
        		try {
        			return requestService.getAll(connection);
        		} catch (RuntimeException e) {
        			log.error("Unable to retrieve Requests from the API service! Error was: "+e.getMessage());
        			e.printStackTrace();
				}
        	}
        	
        	// Parent Type: CertificateFolder -> Relation: Certificates - defined in ModuleBuilder class.
        	if(parent.isOfType("CertificateFolder") && relationName.equals("Certificates")) {
        		log.debug("findChildrenInRelation - Creating CERTS for Connection ID ["+connectionId+"]"); 
        		try {
        			return certificateService.getAllCertificates(connection);
        		} catch (RuntimeException e) {
        			log.error("Unable to retrieve Certificates from the API service! Error was: "+e.getMessage());
        			e.printStackTrace();
				}
        	}
        	
        	// Parent Type: CredentialFolder -> Credentials
        	if(parent.isOfType("CredentialFolder") && relationName.equals("Credentials")) {
        		log.debug("findChildrenInRelation - Creating CREDS for Connection ID ["+connectionId+"]"); 

        		try {
        			return credentialService.getAll(connection);
        		} catch (RuntimeException e) {
        			log.error("Unable to retrieve Credentials from the API service! Error was: "+e.getMessage());
        			e.printStackTrace();
				}
        	}
        	
        	// Parent Type: Environment -> Relation: ProductX
        	if(parent.isOfType("Environment")) {
        		log.debug("findChildrenInRelation - Getting Products for Environment ["+parent.getId()+"]");
        		
        		// Get the environment object.
        		Environment env = environmentService.getByValue(connection, parent.getId().split("@")[0]);
        		
           		List<BaseProduct> products = new ArrayList<BaseProduct>();
        		
           		// Get products from environment, see if they match the type in the relation
           		// Each Environment can only have one instance of the Product, so the ResourceID@ConnectionID will always be the same - the only difference is the relationName, which in this case we use the same value as the Product class names.
        		for (BaseProduct product: env.getProducts()) {
        			log.debug("Checking for Product ["+product.getClass().getSimpleName()+"] in ["+product.getInternalId()+"]");
        			if(product.getClass().getSimpleName().equals(relationName)) {
        				log.debug("Found product ["+product.getInternalId()+"] of type ["+product.getClass().getSimpleName()+"] in the environment matching type ["+relationName+"], adding it");
        				products.add(product);
        			} else {
        				log.debug("Product ["+product.getInternalId()+"] of type ["+product.getClass().getSimpleName()+"] did not match type ["+relationName+"], continuing");
        			}
        		}
        		log.debug("Product count for type : "+products.size());
        		return products;
        	}
        	
        	// Parent Type: Product -> Relation: ProductNode
        	// Product nodes are generic objects but specific to their implementation.
        	if(relationName.equals("ProductNodes")) {
        		switch(parent.getType()) {
        		case "AutomationProduct":
        			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "vra").getProductNodes());
        		case "IdentityManagerProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "vidm").getProductNodes());
        		case "LogInsightProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "vrli").getProductNodes());
        		case "NetworkInsightProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "vrni").getProductNodes());
        		case "OperationsProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "vrops").getProductNodes());
        		case "NetworkInsightProxyProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "vrnicloudproxy").getProductNodes());
        		case "OperationsProxyProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "vropscloudproxy").getProductNodes());
        		case "OrchestratorProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "vro").getProductNodes());
        		case "SaltStackProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "vssc").getProductNodes());
        		case "BusinessCloudProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "vrb").getProductNodes());
        		case "CloudProxyProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "cloudproxy").getProductNodes());
        		case "ExtensibilityProxyProduct":
	    			return Arrays.asList(environmentService.getSpecificProduct(connection, parent.getId().split("@")[0], "abxcloudproxy").getProductNodes());
	    		default:
	    			log.error("Product Node findChildren not implemented for "+parent.getType());
	    			break;
        		}
        	}
        }
    	log.debug("findChildrenInRelation - Ending - InventoryRef ["+parent.getType()+":"+parent.getId()+"] Relation Name ["+relationName+"]");
		return null;
    }

}