package com.sprockitconsulting.vrslcm.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sprockitconsulting.vrslcm.plugin.component.ObjectFactory;
import com.sprockitconsulting.vrslcm.plugin.endpoints.ConnectionRepository;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Certificate;
import com.sprockitconsulting.vrslcm.plugin.scriptable.CertificateFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.ContentManagementFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.CredentialFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Datacenter;
import com.sprockitconsulting.vrslcm.plugin.scriptable.DatacenterFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
import com.sprockitconsulting.vrslcm.plugin.scriptable.EnvironmentFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Folder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.InventoryError;
import com.sprockitconsulting.vrslcm.plugin.scriptable.LifecycleOperationsFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.LockerFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.sprockitconsulting.vrslcm.plugin.scriptable.RequestFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.UserManagementFolder;
import com.sprockitconsulting.vrslcm.plugin.scriptable.VirtualCenter;
import com.vmware.o11n.plugin.sdk.spring.AbstractSpringPluginFactory;
import com.vmware.o11n.plugin.sdk.spring.InventoryRef;

import ch.dunes.vso.sdk.api.QueryResult;

/**
 * This class is responsible for the core lookup logic of all Findable types in the Inventory.
 * The key methods used are:
 * 		* find() - called anytime a VsoFinder annotated class needs to be deserialized
 * 		* findRootChildrenInRelation() - called when the Inventory 'root' finder is invoked
 * 		* findChildrenInRelation() - called anytime a VsoFinder annotated class has a relation, so as to return a list of objects.
 * 
 * @author justin
 *
 */
public final class vRSLCMPluginFactory extends AbstractSpringPluginFactory {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(vRSLCMPluginFactory.class);

	// Used to retrieve Connections and ObjectFactory objects stored in Orchestrator as needed.
	@Autowired
	private ConnectionRepository repository;
	
	/**
	 * This method is called when the Finder is invoked to deserialize the object for the specified type.
	 * You can reproduce this by expanding a variable of the type to show the properties in a Orchestrator variable.
	 * In this plugin for remote objects, the Id inside of InventoryRef follows this format:
	 * 		[Resource Type]:[Resource ID]@[Connection ID]
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
    		// For these find requests, you will use the ObjectFactory associated to the Connection ID.
        	// The 'internalId' of resources contains this information.
        	String resourceId = ref.getId().split("@")[0]; // Should be the resource ID.
        	String connectionId = ref.getId().split("@")[1]; // Should be connection
        	
        	// Now that you know the connectionId, you can get the ObjectFactory from the repository.
        	ObjectFactory objectFactory = repository.findObjectFactory(connectionId);
        	
        	// Now, based on the InventoryRef 'type' - run the appropriate method from the factory to get what you need.
	    	if(ref.isOfType("Datacenter")) {
	        	return objectFactory.getDatacentersByNameOrId(resourceId);
	        } 
	    	
	    	if(ref.isOfType("Environment")) {
	    		return objectFactory.getEnvironmentsByNameOrId(resourceId);
	    	} 
	    	
	    	if(ref.isOfType("Request")) {
	    		return objectFactory.getRequestByNameOrId(resourceId);
	    	}
	    	
	    	if(ref.isOfType("Certificate")) {
	    		return objectFactory.getCertificateById(resourceId);
	    	}
        }
        return null;
    }

    /**
     * Looks up all of the resources based on the specified type and optional query.
     * This is used in workflow fields leveraging an input that is set to "Value Picker"
     * TODO: Is this used? findAll Query returns no value. Outside of connections, seems to have no point.
     */
    @Override
    public QueryResult findAll(String type, String query) {
    	log.debug("Factory : calling findAll() for TYPE ["+type+"] and QUERY ["+query+"]");
    	
    	QueryResult results = new QueryResult();
    	
    	// Handle each type. Connections are 'local' to vRO's EndpointConfigurationService.
    	switch(type) {
    		case "Connection":
    			// Find the Connections in the live repository to return as Inventory objects.
    			for (Connection connection : repository.findAll() ) {
    				log.debug("findAll() adding "+type+" ["+connection.getName()+":"+connection.getId()+"] to the QueryResult.");
    				results.addElement(connection);
    			}
      			log.debug("findAll() - QueryResult object "+type+" :: Count ["+results.getTotalCount()+"], Elements ["+results.getElements().toString() );
    			break;
    		// for each below, work with the ObjectFactory.
    		case "Datacenter":
    			log.debug("findAll() reached the datacenters! Type ["+type+"], Query ["+query+"]");
    			//LifecycleOperationsDatacenterService service = new LifecycleOperationsDatacenterService();
    			// in this case I would call the service.getAllDatacenters()
    		case "Environment":
    			// in this case I would call the service.getAllEnvironments()
    		case "Request":
    			// in this case I would call the service.getAllRequests()
    		case "VirtualCenter":
    			// in this case I would call the service.getVirtualCentersForDatacenter('someId')
    		default:
    			// Put some warning in the log just in case.
    			log.warn("findAll() could not handle a search for the type ["+type+"] in the factory, a case should be added for it");
    	}
    	return results;
    }

    /**
     * This method is called in the Orchestrator Inventory screen when enumerating the 'initial' children from the Plugin.
     * In the vRSLCM Plugin case, this would be a Connection object.
     */
    @Override
    public List<?> findChildrenInRootRelation(String type, String relationName) {
    	log.debug("findChildrenInRootRelation() - ["+type+"]/["+relationName+"] starting");
    	List<Connection> connections = new ArrayList<>();
    	
    	// ConnectionRepository has this information populated at startup, so retrieve the list from there.
    	for (Connection connection : repository.findAll()) {
    		log.debug("Factory found Connection ["+connection.getId()+"] in repository");
    		try {
    			log.debug("Factory found ObjectFactory for Connection ID ["+connection.getId()+"] - "+repository.findObjectFactory(connection.getId()).toString());
    		} catch (RuntimeException e) {
    			log.warn("Couldn't get the ObjectFactory for Connection ID ["+connection.getId()+"]");
    		}
			connections.add(connection);
    	}
    	log.debug("findChildrenInRootRelation() returned ["+connections.size()+"] values for  - ["+type+"]/["+relationName+"] .");
    	return connections;
    }
    
    /**
     * This method is called for any Inventory items configured to have relationships in the tree.
     * These are created in the class as a VsoFinder (thus creating a Findable Type)
     * The relationships are managed in the vRSLCMModuleBuilder class.
     */
	@Override
    public List<?> findChildrenInRelation(InventoryRef parent, String relationName) {
    	log.debug("findChildrenInRelation - Starting - InventoryRef ["+parent.getType()+"] ["+parent.getId()+"] Relation Name ["+relationName+"]");
    	
    	// Specify the ConnectionId for all child objects to create necessary association to the connection that created them.
     	String connectionId;
        if(parent.getId().indexOf("@") != -1) {
        	// @ is present, split it.
        	connectionId = parent.getId().split("@")[1];
        } else {
        	// not present
        	connectionId = parent.getId();
        }

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
	    		if(relationName.equals("DatacenterFolders") ) {
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
	    		if(relationName.equals("CertificateFolders") ) {
	    			lockerFolders.add(new CertificateFolder(connectionId) );
	    		} else if(relationName.equals("CredentialFolders") ) {
	    			lockerFolders.add(new CredentialFolder(connectionId) );
	    		} 
	 		} catch (Exception e) {
	 			log.error("Unable to construct first level Locker folders with Connection ID ["+connectionId+"] - Exception was "+e.toString());
	 		}
	 		return lockerFolders;
    	} else if(parent.isOfType("ContentManagementFolder")) {	
    		InventoryError nyi = new InventoryError("Content Management NYI", "NYI", "NYI");
    		ArrayList<InventoryError> err = new ArrayList<InventoryError>();
    		err.add(nyi);
    		return err;
    	} else if(parent.isOfType("UserManagementFolder")) {
    		InventoryError nyi = new InventoryError("User Management NYI", "NYI", "NYI");
    		ArrayList<InventoryError> err = new ArrayList<InventoryError>();
    		err.add(nyi);
    		return err;
    	} else {
    		// These are objects that require an API connection be established and the results returned.
    		// To ensure multiple Connections are supported, each Object is associated with the Connection ID value.
    		// Each Connection has a corresponding ObjectFactory instance which is responsible for the core lookup logic.
    		ObjectFactory objectFactory = repository.findObjectFactory(connectionId);
    		log.debug("Factory - ObjectFactory ["+objectFactory.toString()+"] loaded for connection ID ["+connectionId+"]");
    		
    		// Parent Type: DatacenterFolder -> Relation: Datacenters - defined in ModuleBuilder class.
        	if(parent.isOfType("DatacenterFolder") && relationName.equals("Datacenters")) {
        		log.debug("findChildrenInRelation - Creating DATACENTERS for Connection ID ["+connectionId+"]");    			
        		List<Datacenter> datacenters = new ArrayList<Datacenter>();

        		// Lookup datacenters in the ObjectFactory.
        		try {
        			datacenters = Arrays.asList(objectFactory.getAllDatacenters());
        			return datacenters;
        		} catch (RuntimeException e) {
					// In case of an issue, return an Error type for the UI experience
					List<InventoryError> error = new ArrayList<InventoryError>();
					InventoryError err = new InventoryError();
					err.setErrorName("Datacenter Lookup Error");
					err.setErrorDescription(e.getLocalizedMessage());
					err.setErrorCode(e.getCause().toString());
					error.add(err);
					return error;
				}
        	} 
        	// Parent type: Datacenter -> Relation: VirtualCenters - defined in ModuleBuilder class.
        	if(parent.isOfType("Datacenter") && relationName.equals("VirtualCenters")) {
        		String dcId = parent.getId().split("@")[0];
        		log.debug("findChildrenInRelation - Creating VIRTUALCENTERS for Connection ID ["+connectionId+"] in Datacenter ["+dcId+"]");    			
        		List<VirtualCenter> vcs = new ArrayList<VirtualCenter>();
        		
        		// Lookup vCenters in the ObjectFactory based on the specified Datacenter.
        		try {
        			vcs = Arrays.asList(objectFactory.getAllVirtualCentersInDatacenter(dcId));
        			return vcs;
        		} catch (RuntimeException e) {
					// In case of an issue, return an Error type for the UI experience					
        			VirtualCenter vc = new VirtualCenter();
					vc.setName("vCenter Lookup Error");
					vcs.add(vc);
					return vcs;
				}
        	}
        	
        	// Parent Type: EnvironmentFolder -> Relation: Environments - defined in ModuleBuilder class.
        	if(parent.isOfType("EnvironmentFolder") && relationName.equals("Environments")) {
        		log.debug("findChildrenInRelation - Creating ENVIRONMENTS with Reference ID ["+connectionId+"]"); 
        		List<Environment> environments = new ArrayList<Environment>();
        		
        		// Lookup environments in the ObjectFactory.
        		try {
        			environments = Arrays.asList(objectFactory.getAllEnvironments());
        			return environments;
        		} catch (RuntimeException e) {
					// In case of an issue, return an Error type for the UI experience
        			Environment env = new Environment();
					env.setName("Environment Error");
					env.setResourceId(UUID.randomUUID().toString());
					environments.add(env);
					return environments;
				}
        	}

        	// RequestFolder -> Requests
        	if(parent.isOfType("RequestFolder") && relationName.equals("Requests")) {
        		log.debug("findChildrenInRelation - Creating REQUESTS with Reference ID ["+connectionId+"]"); 
        		List<Request> requests = new ArrayList<Request>();
        		
        		// Lookup requests in the ObjectFactory.
        		try {
        			requests = Arrays.asList(objectFactory.getAllRequests());
        			return requests;
        		} catch (RuntimeException e) {
					// In case of an issue, return an Error type for the UI experience
        			Request req = new Request();
					req.setName("Request Lookup Error");
					req.setResourceId(UUID.randomUUID().toString());
					requests.add(req);
					return requests;
				}
        	}
        	
        	// Parent Type: CertificateFolder -> Relation: Certificates - defined in ModuleBuilder class.
        	if(parent.isOfType("CertificateFolder") && relationName.equals("Certificates")) {
        		log.debug("findChildrenInRelation - Creating CERTS with Reference ID ["+connectionId+"]"); 
        		List<Certificate> certs = new ArrayList<Certificate>();
        		
        		// Lookup requests in the ObjectFactory.
        		try {
        			certs = Arrays.asList(objectFactory.getAllCertificates());
        			return certs;
        		} catch (RuntimeException e) {
					// In case of an issue, return an Error type for the UI experience
        			Certificate cert = new Certificate();
					cert.setAlias("Certificate Lookup Error");
					cert.setResourceId(UUID.randomUUID().toString());
					certs.add(cert);
					return certs;
				}
        	}
        }
    	log.debug("findChildrenInRelation - Ending - InventoryRef ["+parent.getType()+":"+parent.getId()+"] Relation Name ["+relationName+"]");
		return null;
    }

}