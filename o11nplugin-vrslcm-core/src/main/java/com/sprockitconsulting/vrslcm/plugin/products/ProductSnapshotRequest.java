package com.sprockitconsulting.vrslcm.plugin.products;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class is used to hold the data required during a Snapshot request for a Product.
 */
public class ProductSnapshotRequest {

	private String description;
	private String snapshotPrefix;
	private Boolean shutdownBeforeSnapshot = false;
	private Boolean snapshotWithMemory = false;
	
	public ProductSnapshotRequest() {}

	public ProductSnapshotRequest(String description, String snapshotPrefix, Boolean shutdownBeforeSnapshot, Boolean snapshotWithMemory) {
		this.description = description;
		this.snapshotPrefix = snapshotPrefix;
		this.shutdownBeforeSnapshot = shutdownBeforeSnapshot;
		this.snapshotWithMemory = snapshotWithMemory;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSnapshotPrefix() {
		return snapshotPrefix;
	}

	public void setSnapshotPrefix(String snapshotPrefix) {
		this.snapshotPrefix = snapshotPrefix;
	}

	public Boolean getShutdownBeforeSnapshot() {
		return shutdownBeforeSnapshot;
	}

	public void setShutdownBeforeSnapshot(Boolean shutdownBeforeSnapshot) {
		this.shutdownBeforeSnapshot = shutdownBeforeSnapshot;
	}

	public Boolean getSnapshotWithMemory() {
		return snapshotWithMemory;
	}

	public void setSnapshotWithMemory(Boolean snapshotWithMemory) {
		this.snapshotWithMemory = snapshotWithMemory;
	}
	
	
}
