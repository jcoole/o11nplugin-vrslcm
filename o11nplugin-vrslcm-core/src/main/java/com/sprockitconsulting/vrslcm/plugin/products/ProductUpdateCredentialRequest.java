package com.sprockitconsulting.vrslcm.plugin.products;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sprockitconsulting.vrslcm.plugin.scriptable.Credential;

public class ProductUpdateCredentialRequest {

	@JsonProperty("currentAdminPassword")
	private Credential current;
	@JsonProperty("adminPassword")
	private Credential updated;
	
	public ProductUpdateCredentialRequest() {}

	public ProductUpdateCredentialRequest(Credential current, Credential updated) {
		this.current = current;
		this.updated = updated;
	}

	public Credential getCurrent() {
		return current;
	}

	public void setCurrent(Credential current) {
		this.current = current;
	}

	public Credential getUpdated() {
		return updated;
	}

	public void setUpdated(Credential updated) {
		this.updated = updated;
	}
}
