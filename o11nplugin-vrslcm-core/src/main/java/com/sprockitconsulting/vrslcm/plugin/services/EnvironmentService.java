package com.sprockitconsulting.vrslcm.plugin.services;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Environment;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
import com.sprockitconsulting.vrslcm.plugin.dao.DaoEnvironment;
import com.sprockitconsulting.vrslcm.plugin.products.AbstractProduct;
import com.sprockitconsulting.vrslcm.plugin.products.ProductNode;
/**
 * This service governs access to the LCM Environment resources and related objects.
 * It is autowired into the LifecycleOperationsService, used in Orchestrator for users to manage objects.
 * @author justin
 */
@Service
public class EnvironmentService extends AbstractService { 
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(EnvironmentService.class);
	
	@Autowired
	private DaoEnvironment dao;
	
	public EnvironmentService() {
		log.debug("EnvironmentService initialized");
	}
	
	public Environment getByValue(Connection connection, String value) {
		return dao.findById(connection, value);
	}

	public List<Environment> getAll(Connection connection) {
		return dao.findAll(connection);
	}

	// Product and Node specific methods.
	// Since these require deserialization to have taken effect, these will call the 'findById' method first before processing.
	
	/**
	 * Retrieves a specific product from the list.
	 * @param connection The LCM Connection
	 * @param environmentId Environment ID to search
	 * @param productId The Product ID, such as 'vra', 'vidm'
	 * @return The Product and its specifications
	 */
	public <T extends AbstractProduct> T getSpecificProduct(Connection connection, String environmentId, String productId) {
		if(connection == null) {
			throw new RuntimeException("You must specify a Connection!");
		}
		if(productId == null || productId.isBlank() || environmentId == null || environmentId.isBlank()) {
			throw new RuntimeException("You must specify both an Environment ID and Product ID in the search!");
		}
		
		T[] products = getByValue(connection, environmentId).getProducts();
		// There can be only one product of specified type per Environment.
		T product = (T) Arrays.stream(products).filter(p -> p.getProductId().equals(productId)).findFirst().orElse(null);
		if(product == null) {
			log.warn("Unable to find specified Product ["+productId+"] in Environment ID ["+environmentId+"]!");
		}
		return (T) product;
	}
	
	/**
	 * Retrieve a specific product's node from the list.
	 * @param connection The LCM Connection
	 * @param environmentId Environment ID to search
	 * @param productId The Product ID, such as 'vra', 'vidm'
	 * @param type The Node Type
	 * @param name The Node VM Name
	 * @return The Product Node and its specifications
	 */
	public ProductNode getSpecificProductNode(Connection connection, String environmentId, String productId, String type, String name) {
		if(connection == null) {
			throw new RuntimeException("You must specify a Connection!");
		}
		if(environmentId == null || environmentId.isBlank() || productId.isBlank() || productId == null || type.isBlank() || type == null || name.isBlank() || name == null) {
			throw new RuntimeException("You must specify the Environment ID, Product ID, Node type, and Name! One or more of the values are missing.");
		}
		ProductNode productNode = null;
		
		// Get the product
		AbstractProduct product = null;
		try {
			product = getSpecificProduct(connection, environmentId, productId);
			log.debug("Got the Product ["+productId+"] -- "+product.toString());
		} catch (RuntimeException e) {
			log.error("Issue retrieving the Product ["+productId+"] from environment: "+e.getMessage());
		}
		
		// Filter the node(s) by name and type.
		productNode = Arrays.stream(product.getProductNodes()).filter(n -> n.getType().equals(type) && n.getProductNodeSpec().getNodeProperty("vmName").equals(name)).findFirst().orElse(null);
		return productNode;
	}
	
	/**
	 * Requests an inventory sync operation on the environment.
	 * @param connection The LCM Connection
	 * @param environmentId Environment ID to sync
	 * @return The Requests generated for each Product that is installed
	 */
	public List<Request> getEnvironmentSyncRequest(Connection connection, String environmentId) {
		return dao.environmentSyncRequest(connection, environmentId);
	}
	
	// Product Actions
	public Request executePowerOn(Connection connection, String environmentId, String productId) {
		log.debug("executePowerOn() - "+connection+", "+environmentId+", "+productId);
		return dao.powerOnRequest(connection, environmentId, productId);
	}
	
	public Request executePowerOff(Connection connection, String environmentId, String productId) {
		log.debug("executePowerOff() - "+connection+", "+environmentId+", "+productId);
		return dao.powerOffRequest(connection, environmentId, productId);
	}
	
	/** NYI
	 * 
	 * @param connection
	 * @param environmentId
	 * @param productId
	 * @param description
	 * @param prefix
	 * @param memory
	 * @param shutdown
	 * @return
	 */
	public Request executeCreateSnapshot(Connection connection, String environmentId, String productId, String description, String prefix, Boolean memory, Boolean shutdown) {
		return dao.createSnapshotRequest(connection, environmentId, productId, description, prefix, memory, shutdown);
	}
	


}
