package com.doubleclue.dcem.as.logic;

public class RegisteredDomain {
	
	private boolean isReverseProxy;
	private String remarks;
	private String companyName;
	private int maxConnections;
	private int registrationEntityId;
	
	public RegisteredDomain(boolean isReverseProxy, String remarks, String companyName, int maxConntions, int registrationEntityId) {
		super();
		this.isReverseProxy = isReverseProxy;
		this.remarks = remarks;
		this.companyName = companyName;
		this.maxConnections = maxConntions;
		this.registrationEntityId = registrationEntityId;
	}
	
	public boolean isReverseProxy() {
		return isReverseProxy;
	}
	public void setReverseProxy(boolean isReverseProxy) {
		this.isReverseProxy = isReverseProxy;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public String getCompanyName() {
		return companyName;
	}
	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public int getRegistrationEntityId() {
		return registrationEntityId;
	}

	public void setRegistrationEntityId(int registrationEntityId) {
		this.registrationEntityId = registrationEntityId;
	}

}
