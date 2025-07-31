package com.sprockitconsulting.vrslcm.plugin.products;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprockitconsulting.vrslcm.plugin.scriptable.BaseLifecycleManagerObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoObject;
import com.vmware.o11n.plugin.sdk.annotation.VsoProperty;

/**
 * Represents a snapshot in the LCM Inventory for a Product.
 * LCM uses a custom field in vCenter to keep track of these, with key '48' and the value is a JSON representation of the Snapshot.
 * Note that Snapshots in LCM are managed separate to vSphere snapshots, even if you sync inventory on the product.
 * It's also not possible to 'delete all' at once - each must be done in separate requests.
 * A workflow can do this logically but it may take a while!
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@VsoObject(description = "Represents a LCM-managed snapshot for the Product.")
public class ProductSnapshot extends BaseLifecycleManagerObject {

	// Enable Logging
	private static final Logger log = LoggerFactory.getLogger(ProductSnapshot.class);
	
	private final String name;
	private final String description;
	private final long generatedTime; // timestamp value just like Request - 1687911210934
	private final String generatedTimeValue; 
	private final Boolean withMemory;
	private final Boolean quiesce;
	private final Boolean shutdown;
	private final Boolean isCurrent;
	private final String parentSnapshotId;
	private final String status; // INVALID, VALID
	private final List<ProductSnapshot> childrenSnapshots;
	private String environmentId;
	private String productId;


	/**
	 * @param name
	 * @param description
	 * @param generatedTime
	 * @param withMemory
	 * @param quiesce
	 * @param shutdown
	 * @param isCurrent
	 * @param parentSnapshotId
	 * @param status
	 * @param childrenSnapshots
	 */
	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public ProductSnapshot(
			@JsonProperty("snapshotName")String name, 
			@JsonProperty("snapshotDescription")String description, 
			@JsonProperty("snapshotGeneratedTime")long generatedTime, 
			@JsonProperty("snapshotWithMemory")Boolean withMemory, 
			@JsonProperty("quiesceSnapshot")Boolean quiesce,
			@JsonProperty("shutdownSnapshot")Boolean shutdown, 
			@JsonProperty("currentSnapshot")Boolean isCurrent, 
			@JsonProperty("parentSnapshotId")String parentSnapshotId, 
			@JsonProperty("status")String status, 
			@JsonProperty("childrenSnapshots")List<ProductSnapshot> childrenSnapshots) {
		super();
		this.name = name;
		this.description = description;
		this.generatedTime = generatedTime;
		this.generatedTimeValue = Instant.ofEpochMilli(generatedTime).toString();
		this.withMemory = withMemory;
		this.quiesce = quiesce;
		this.shutdown = shutdown;
		this.isCurrent = isCurrent;
		this.parentSnapshotId = parentSnapshotId;
		this.status = status;
		this.childrenSnapshots = childrenSnapshots;
		log.debug("Snapshot created: "+this.toString());
	}

	@JsonProperty("snapshotId")
	@Override
	public void setResourceId(String id) {
		this.resourceId = id;
	}
	
	@VsoProperty(description = "Name of the Snapshot.")
	public String getName() {
		return name;
	}
	
	@VsoProperty(description = "Description of the Snapshot.")
	public String getDescription() {
		return description;
	}
	
	public long getGeneratedTime() {
		return generatedTime;
	}
	
	@VsoProperty(description = "Time value of when the Snapshot was created.")
	public String getGeneratedTimeValue() {
		return generatedTimeValue;
	}
	
	@VsoProperty(description = "Whether or not the Snapshot also captured memory, making it a 'point in time' for revert purposes.")
	public Boolean getWithMemory() {
		return withMemory;
	}
	
	@VsoProperty(description = "Whether or not the data is/was quiesced when the Snapshot was created.")
	public Boolean getQuiesce() {
		return quiesce;
	}
	
	@VsoProperty(description = "Whether or not the machine was shut down prior to the Snapshot being created.")
	public Boolean getShutdown() {
		return shutdown;
	}
	
	@VsoProperty(description = "Indicates that this Snapshot is currently the one used.")
	public Boolean getIsCurrent() {
		return isCurrent;
	}
	
	@VsoProperty(description = "The Resource ID of the current snapshot object's logical parent snapshot in the tree. Use this value to target snapshots to be deleted.")
	public String getParentSnapshotId() {
		return parentSnapshotId;
	}
	
	@VsoProperty(description = "The status of the Snapshot, either VALID or INVALID. The latter usually means a snapshot was removed directly from vCenter, and this object is just stale metadata.")
	public String getStatus() {
		return status;
	}

	@VsoProperty(description = "Returns the list of descendent snapshots, if they exist.")
	public List<ProductSnapshot> getChildrenSnapshots() {
		return childrenSnapshots;
	}
	
	@VsoProperty(description = "The Environment ID associated with the snapshot.")
	public String getEnvironmentId() {
		return environmentId;
	}

	public void setEnvironmentId(String environmentId) {
		this.environmentId = environmentId;
	}
	@VsoProperty(description = "The Product associated with the snapshot.")
	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	@Override
	public String toString() {
		return String.format(
				"ProductSnapshot [name=%s, description=%s, generatedTime=%s, generatedTimeValue=%s, withMemory=%s, quiesce=%s, shutdown=%s, isCurrent=%s, parentSnapshotId=%s, status=%s, childrenSnapshots=%s, environmentId=%s, productId=%s]",
				name, description, generatedTime, generatedTimeValue, withMemory, quiesce, shutdown, isCurrent,
				parentSnapshotId, status, childrenSnapshots, environmentId, productId);
	}
	





}
