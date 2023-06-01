package com.sprockitconsulting.vrslcm.plugin.dao;

import com.sprockitconsulting.vrslcm.plugin.scriptable.Connection;

public interface IDaoUpdate<T> {
	T update(final Connection connection, final T original, final T replacement);
	
}
