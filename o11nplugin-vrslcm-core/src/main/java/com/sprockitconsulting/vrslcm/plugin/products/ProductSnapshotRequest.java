package com.sprockitconsulting.vrslcm.plugin.products;

/**
 * This class is used to hold the data required during a Snapshot request for a Product.
 * @see
 * {@link com.sprockitconsulting.vrslcm.plugin.dao.DaoEnvironment#createSnapshotRequest() Create Snapshot}
 * {@link com.sprockitconsulting.vrslcm.plugin.dao.DaoEnvironment#deleteSnapshotRequest() Delete Snapshot}
 */
public class ProductSnapshotRequest {

	public ProductSnapshotRequest() {}
	
	// Static class to hold the Creation request payload.
	public static class Create {
		private String description;
		private String snapshotPrefix;
		private Boolean shutdownBeforeSnapshot = false;
		private Boolean snapshotWithMemory = false;
		
		public Create(String description, String snapshotPrefix, Boolean shutdownBeforeSnapshot, Boolean snapshotWithMemory) {
			this.description = description;
			this.snapshotPrefix = snapshotPrefix;
			this.shutdownBeforeSnapshot = shutdownBeforeSnapshot;
			this.snapshotWithMemory = snapshotWithMemory;
		}

		public String getDescription() {
			return description;
		}

		public String getSnapshotPrefix() {
			return snapshotPrefix;
		}

		public Boolean getShutdownBeforeSnapshot() {
			return shutdownBeforeSnapshot;
		}

		public Boolean getSnapshotWithMemory() {
			return snapshotWithMemory;
		}
		
		
	}
	
	// Static class to hold the values needed for a Snapshot Delete request.
	public static class Delete {
		private String productSnapshotId;
		private final Boolean consolidate = true;
		
		public Delete(String productSnapshotId) {
			this.productSnapshotId = productSnapshotId;
		}
		
		public String getProductSnapshotId() {
			return productSnapshotId;
		}
		public Boolean getConsolidate() {
			return consolidate;
		}
		
	}
	
}
