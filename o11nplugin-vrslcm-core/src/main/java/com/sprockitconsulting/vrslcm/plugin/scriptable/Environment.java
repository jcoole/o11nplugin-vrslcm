package com.sprockitconsulting.vrslcm.plugin.scriptable;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprockitconsulting.vrslcm.plugin.products.AbstractProduct;
import com.sprockitconsulting.vrslcm.plugin.products.ProductNode;
import com.sprockitconsulting.vrslcm.plugin.services.EnvironmentService;
import com.vmware.o11n.plugin.sdk.annotation.Cardinality;
import com.vmware.o11n.plugin.sdk.annotation.VsoFinder;
import com.vmware.o11n.plugin.sdk.annotation.VsoMethod;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;
import com.vmware.o11n.plugin.sdk.annotation.VsoRelation;

/**
 * Represents an Environment in LCM.
 * @author justin
 */
@Component
@Scope(value = "prototype")
@JsonIgnoreProperties(ignoreUnknown = true) // if a field isn't mapped to a JSON property, skip it
@JsonInclude(JsonInclude.Include.NON_NULL)
@VsoObject(description = "Represents a vRSLCM Environment, containing Products and their Nodes/Servers.")
//Creating the VsoFinder exposes this in the 'Types' section of API Explorer.
@VsoFinder(
	name = "Environment", // Type name!!! This value actually translates to 'type' in VSO.XML!!
	description = "A vRSLCM Environment configuration.", // shows up in the 'Types'
	idAccessor = "getInternalId()", // method in the class to use for specific lookup
	image = "images/vrslcm-env.png", // relative path to image in inventory use
	relations = {
		@VsoRelation(name = "IdentityManagerProduct", type = "IdentityManagerProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "AutomationProduct", type = "AutomationProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "LogInsightProduct", type = "LogInsightProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "OperationsProduct", type = "OperationsProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "NetworkInsightProduct", type = "NetworkInsightProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "OperationsProxyProduct", type = "OperationsProxyProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "NetworkInsightProxyProduct", type = "NetworkInsightProxyProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "OrchestratorProduct", type = "OrchestratorProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "SaltStackProduct", type = "SaltStackProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "BusinessCloudProduct", type = "BusinessCloudProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "CloudProxyProduct", type = "CloudProxyProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE),
		@VsoRelation(name = "ExtensibilityProxyProduct", type = "ExtensibilityProxyProduct", inventoryChildren = true, cardinality = Cardinality.TO_ONE)
	}
)
public class Environment extends BaseLifecycleManagerObject {
	
	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(Environment.class);
	
	@Autowired
	private EnvironmentService environmentService;
	
	private String name;
	private EnvironmentInfrastructure infrastructure;
	private Datacenter datacenter;
	private AbstractProduct[] products;
	
	public Environment() {
		super();
	}
	
	@VsoProperty(description = "Name of the Environment")
	public String getName() {
		return name;
	}
	
	@JsonProperty("environmentName")
	public void setName(String name) {
		this.name = name;
	}
	
	@VsoProperty(description = "ID of the Environment", readOnly = true)
	public String getResourceId() {
		return resourceId;
	}
	
	@JsonProperty("environmentId")
	@JsonAlias("vmid") // TODO: this may not be correct, depending on the payload. check this.
	@Override
	public void setResourceId(String id) {
		this.resourceId = id;
	}

	@VsoProperty(description = "The Datacenter that the Environment is deployed in.")
	public Datacenter getDatacenter() {
		return datacenter; // TODO: change this to be a lookup using the infra property dataCenterVmid
	}

	public void setDatacenter(Datacenter datacenter) {
		this.datacenter = datacenter;
	}

	@VsoProperty(description = "Infrastructure-related properties for the Environment.", showInColumn = false)
	public EnvironmentInfrastructure getInfrastructure() {
		return infrastructure;
	}
	
	@JsonProperty("infrastructure")
	public void setInfrastructure(EnvironmentInfrastructure infrastructure) {
		this.infrastructure = infrastructure;
	}
	
	@SuppressWarnings("unchecked")
	@VsoProperty(description = "The Products that are deployed in the specified environment.")
	public <T extends AbstractProduct> T[] getProducts() {
		return (T[]) products;
	}
	
	@JsonProperty("products")
	public <T extends AbstractProduct> void setProducts(T[] products) {
		this.products = (AbstractProduct[]) products;
	}
	
	@VsoMethod(description = "Trigger Inventory Sync on the Environment. A separate Request object is returned for each Product in the environment.")
	public List<Request> triggerInventorySync() {
		return environmentService.getEnvironmentSyncRequest(this.getConnection(), this.getResourceId());
	}
	
	/**
	 * Used to update Product and Node internalId values once the Connection is assigned.
	 * Since Products and their nodes are directly attached to Environments and not independently manageable, it's not possible to inject services into them otherwise.
	 * Thus, the environmentService spring bean is assigned when the Environment is deserialized.
	 */
	public void assignFinderIdValuesToProductsAndNodes() {
		EnvironmentService environmentService = (EnvironmentService) context.getBean("environmentService");
		for (AbstractProduct product : getProducts()) {
    		// Products are unique per Environment/Connection. The 'productId' string used in lookup is handled in the Finder methods.
			String productInternalId = resourceId+"@"+connection.getId();
			product.setInternalId(productInternalId);
			product.setConnection(connection);
			product.setEnvironmentId(resourceId);
			product.setEnvironmentService(environmentService);
			log.debug("Assigned Internal ID, Connection, Environment ID values to product ["+productInternalId+"]");
			
			// Nodes internalId follows this format -- [vmName]:[type]:[product]:[environmentId]@[connectionId]
			for (ProductNode node : product.getProductNodes()) {
				String vmName = node.getProductNodeSpec().getNodeProperty("vmName").toString();
				String nodeInternalId = vmName+":"+node.getType()+":"+product.getProductId()+":"+this.resourceId;
				node.setName(vmName+" - "+node.getType());
				node.setInternalId(nodeInternalId);
				node.setConnection(connection);
				node.setEnvironmentService(environmentService);
				log.debug("Assigned internal ID to node ["+nodeInternalId+"]");
			}
		}
	}
	

}