package com.doubleclue.dcem.core.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DomainConfig {

	@JsonProperty("r")
	boolean remote = false;
	
	@JsonProperty("g")
	String groupAttribute = "group";
	
	@JsonProperty("v")
	boolean verifyCertificate = false;
	
	@JsonProperty ("a")
	String authConnectorName = null;
	

	public boolean isRemote() {
		return remote;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	public String getAuthConnectorName() {
		return authConnectorName;
	}

	public void setAuthConnectorName(String authConnectorName) {
		this.authConnectorName = authConnectorName;
	}

	public boolean isVerifyCertificate() {
		return verifyCertificate;
	}

	public void setVerifyCertificate(boolean verifyCertificate) {
		this.verifyCertificate = verifyCertificate;
	}

	public String getGroupAttribute() {
		return groupAttribute;
	}

	public void setGroupAttribute(String groupAttribute) {
		this.groupAttribute = groupAttribute;
	}

}
