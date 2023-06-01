package com.sprockitconsulting.vrslcm.plugin.dao;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;

/**
 * Implementing this interface indicates that the resource can be deleted directly, rather than by a separate endpoint or task.
 * @author justin
 *
 * @param <T>
 */
public interface IDaoDelete<T> {
	Object delete(final Connection connection, final T entity);
}
