package com.sprockitconsulting.vrslcm.plugin.products;

import java.util.List;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Request;
/**
 * Implementing this interface indicates the Product supports LCM Snapshot operations.
 */
public interface ISnapshotSupport {
	Request createSnapshot(String snapshotDescription, String snapshotPrefix, Boolean snapshotMemory, Boolean snapshotShutdown);
	List<ProductSnapshot> getSnapshots();
	Request deleteSnapshot(ProductSnapshot snapshot);
}
