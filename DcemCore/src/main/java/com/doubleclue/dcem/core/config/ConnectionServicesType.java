package com.doubleclue.dcem.core.config;

public enum ConnectionServicesType {
	MANAGEMENT("Management", 8443, true, true, "dcem/mgt", null),
	REST("REST Web-Services", 8001, false, true, "dcem/restApi/{module-Id}", null),
	WEB_SOCKETS("Smart-Device Web-Sockets", 443, true, true, "dcem/ws/appConnection", null),
	RADIUS("RADIUS Authentication", 1812, false, false, "", null),
	RADIUS_ACCOUNTING("RADIUS Accounting", 1813, false, false, "", null),
	SAML("SAML", 8002, false, false, "dcem/saml", ConnectionServicesType.MANAGEMENT),
	OPENN_ID_OAUTH("OpenID - OAUTH", 8003, false, false, "dcem/oauth", ConnectionServicesType.MANAGEMENT),
	HEALTH_CHECK("Health Check", 8004, false, false, "dcem/healthcheck", ConnectionServicesType.MANAGEMENT),
	USER_PORTAL("UserPortal", 8005, false, false, "dcem/userportal", ConnectionServicesType.MANAGEMENT),
 	AZURE_CALLBACK("Azure Callback Service", 8006, false, false, "dcem/azureCallback", ConnectionServicesType.MANAGEMENT);

	public String displayName;
	int port;
	boolean secure;
	boolean enabled;
	String path;
	ConnectionServicesType sameAsConnectionServiceType;

	private ConnectionServicesType(String displayName, int port, boolean secure, boolean enabled, String path,
			ConnectionServicesType sameAsConnectionServiceType) {
		this.displayName = displayName;
		this.port = port;
		this.secure = secure;
		this.enabled = enabled;
		this.path = path;
		this.sameAsConnectionServiceType = sameAsConnectionServiceType;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ConnectionServicesType getSameAsConnectionServiceType() {
		return sameAsConnectionServiceType;
	}

	public void setSameAsConnectionServiceType(ConnectionServicesType sameAsConnectionServiceType) {
		this.sameAsConnectionServiceType = sameAsConnectionServiceType;
	}
}
