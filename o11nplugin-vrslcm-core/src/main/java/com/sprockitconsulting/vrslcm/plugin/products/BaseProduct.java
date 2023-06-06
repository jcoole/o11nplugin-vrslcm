package com.sprockitconsulting.vrslcm.plugin.products;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.sprockitconsulting.vrslcm.plugin.scriptable.BaseLifecycleManagerObject;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * This class is for subclassing products since they are quite similar
 * 
 * Common fields at PRODUCT level (nodes have others)
 * 	- id
 * 	- version
 * 	- certificate
 * 	- nodeSize
 * 	- enableTelemetry
 *  - fipsMode
 * 	
 * VIDM fields (Product)
 * 	- defaultConfigurationUsername
 * 	- vidmDomainName
 * 	- syncGroupMembers
 * 	- isClustered (bool)
 * 	- vidmAdminPassword
 *  - defaultConfigurationPassword
 *  - vidmDBType
 *  - adminEmail
 *  
 * VRA fields (Product)
 * 	- vraK8ServiceCidr
 * 	- vraK8ClusterCidr
 *  - ntp
 *  - timeSyncMode
 *  - monitorWithvROps
 *  - integratewithSddcManager
 *  - sddcManagerHostname
 *  - sddcManagerUsername
 *  - sddcManagerPassword
 *  - vrliLogForwardingConfiguration
 *  
 *  VRA fields (Node)
 *  - type ("vrava-primary")
 *  - properties
 *  	generally a lot of the same stuff as environment/infrastructure, but more VM specific stuff like Moref, rootpassword locker link
 *  	probably best to just have Jackson use JsonAnySetter to get the values, and JsonUnwrapped on the properties to add them to the class
 *  
 *  Class hierarchy to use JsonUnwrapped
 *  Product
 *  	id
 *  	version
 *  	- ProductSpec (aka .properties)
 *  ProductNode
 *  	type
 *  	- ProductNodeSpec (aka .properties)
 *  
 *  From a vRO Perspective - annotate a generic 'getProperty' method for scriptable use.
 *  
 *  BaseProduct should be extended to VidmProduct, AutomationProduct, etc for VsoFinder inventory view
 *  
 * @author justin
 *
 */

/*
 * The @JsonTypeInfo is used to handle subclass deserialization.
 * In this setup when the JSON is returned, it is keying off of the 'id' property of the Product to determine what subclass it is.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "id"
)
/*
 * These are the 'valid' subclasses to use in deserialization.
 * Based on the property of 'id' returned from the API, Jackson will deserialize the response with the appropriate subclass of Product.
 * The subclasses are annotated with @JsonTypeName values used here.
 */
@JsonSubTypes({
    @Type(value = AutomationProduct.class, name = "vra"),
    @Type(value = IdentityManagerProduct.class, name = "vidm"),
    @Type(value = LogInsightProduct.class, name = "vrli"),
    @Type(value = NetworkInsightProduct.class, name = "vrni"),
    @Type(value = OperationsProduct.class, name = "vrops"),
    @Type(value = NetworkInsightProxyProduct.class, name = "vrnicloudproxy"),
    @Type(value = OperationsProxyProduct.class, name = "vropscloudproxy"),
    @Type(value = OrchestratorProduct.class, name = "vro"),
    @Type(value = SaltStackProduct.class, name = "vssc"),
    @Type(value = BusinessCloudProduct.class, name = "vrb"),
    @Type(value = CloudProxyProduct.class, name = "cloudproxy"),
    @Type(value = ExtensibilityProxyProduct.class, name = "abxcloudproxy")
})
@VsoObject(description = "Represents a Product object in LCM.", create = false)
public abstract class BaseProduct extends BaseLifecycleManagerObject {
	
	private String productId; // api ID - vidm, vra, vrli, vrops, vrni, etc. used in the internal ID
	private String productVersion;
	private String name; // Friendly name aka "vRealize Automation", set in extended classes
	private String environmentId;
	
	// Since the 'properties' and 'nodes' of each Product vary widely, these values are exposed via generic methods for Workflow developers to key off of.
	@JsonUnwrapped
	@JsonProperty("properties")
	private ProductSpec productSpec;
	@JsonUnwrapped
	@JsonProperty("nodes")
	private ProductNode[] productNodes;
	
	public BaseProduct() {
		
	}
	
	@VsoProperty(description = "The Product ID", readOnly = true)
	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}
	
	@VsoProperty(description = "Environment ID where the Product is deployed", readOnly = true)
	public String getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(String environmentId) {
		this.environmentId = environmentId;
	}

	@VsoProperty(description = "Version of the Product, usually MAJOR.MINOR.PATCH", readOnly = true)
	public String getProductVersion() {
		return productVersion;
	}
	@VsoProperty(description = "Friendly name of the Product", readOnly = true)
	public String getName() {
		return name;
	}
	
	@VsoProperty(description = "Specifications used in the deployment of the Product.", readOnly = true)
	public ProductSpec getProductSpec() {
		return productSpec;
	}
	
	@VsoProperty(description = "The server nodes associated with this Product deployment.", readOnly = true)
	public ProductNode[] getProductNodes() {
		return productNodes;
	}
	

	@JsonProperty("version")
	public void setProductVersion(String productVersion) {
		this.productVersion = productVersion;
	}

	public void setName(String productName) {
		this.name = productName;
	}

	public void setProductSpec(ProductSpec productSpec) {
		this.productSpec = productSpec;
	}
	
	public void setProductNodes(ProductNode[] productNodes) {
		this.productNodes = productNodes;
	}

	@Override
	public String toString() {
		return String.format(
				"Product [productVersion=%s, name=%s, productSpec=%s]",
				productVersion, name, productSpec);
	}
	
	

	
}
