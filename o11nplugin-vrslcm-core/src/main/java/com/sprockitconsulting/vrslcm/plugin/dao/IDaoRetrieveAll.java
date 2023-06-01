package com.sprockitconsulting.vrslcm.plugin.dao;

import java.util.List;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
/**
 * Implemented by default on all DAO resources.
 * @author justin
 * @see IDaoGeneric<T>
 * @param <T>
 */
public interface IDaoRetrieveAll<T> {
	List<T> findAll(final Connection connection);
}
