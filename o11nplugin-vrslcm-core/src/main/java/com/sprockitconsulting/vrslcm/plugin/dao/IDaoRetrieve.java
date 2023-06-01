package com.sprockitconsulting.vrslcm.plugin.dao;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;

/**
 * Implemented by default on all DAO resources.
 * @author justin
 * @see IDaoGeneric<T>
 * @param <T>
 */
public interface IDaoRetrieve<T> {
	T findById(final Connection connection, final String id);
}
