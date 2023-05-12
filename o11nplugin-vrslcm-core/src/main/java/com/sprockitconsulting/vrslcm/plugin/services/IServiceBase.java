package com.sprockitconsulting.vrslcm.plugin.services;

// Used to make calls to the API in a standard way.
public interface IServiceBase<T> {
	
	T getByNameOrId(String nameOrId);
	T[] getAll();
	T create();
	T delete();
	T update();
	
	String getServiceName();
	String getBaseUriTemplate();
}
