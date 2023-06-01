package com.sprockitconsulting.vrslcm.plugin.dao;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;

/**
 * Implementing this interface indicates the resource can be created directly from the API, rather than created via some other endpoint.
 * @author justin
 *
 * @param <T>
 */
public interface IDaoCreate<T> {
	T create(final Connection connection, final T entity);
	
}
