package com.sprockitconsulting.vrslcm.plugin.dao;

/**
 * This interface implements standard methods common to all resources in the system.
 * All resources are at least readable, and a standard URL value set is needed.
 * @author justin
 *
 * @param <T>
 */
public interface IDaoGeneric<T> extends IDaoRetrieve<T>, IDaoRetrieveAll<T> {
	void setGetAllUrl(String getAllUrl);
	void setGetByValueUrl(String getByValueUrl);
}