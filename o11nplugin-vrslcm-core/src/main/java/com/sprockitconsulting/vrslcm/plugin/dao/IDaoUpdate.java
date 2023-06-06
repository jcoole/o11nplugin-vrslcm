package com.sprockitconsulting.vrslcm.plugin.dao;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;
/**
 * Implementing this interface indicates the resource can be updated directly from the API, rather than some other endpoint.
 * @author justin
 *
 * @param <T>
 */
public interface IDaoUpdate<T> {
	T update(final Connection connection, final T original, final T replacement);
	
}
