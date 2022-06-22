package com.doubleclue.dcem.core.config;

public enum KeyStorePurpose {

	// DO NOT CHANGE THE ORDINAL NUMBERS !

	ROOT_CA ("Root-CA", null),
	Management_CA ("Management GUI" , ConnectionServicesType.MANAGEMENT), 
	RestWebServices_CA ("REST Web Services", ConnectionServicesType.REST), 
	DeviceWebsockets_CA ("Smart Device Web-Sockets", ConnectionServicesType.WEB_SOCKETS),
	Saml_Connection_CA ("SAML Web Connection", ConnectionServicesType.SAML),
	Saml_IdP_CA ("SAML Identity Provider",null),
	OAuth_Connection_CA ("OpenID - OAuth", ConnectionServicesType.OPENN_ID_OAUTH),
	USER_PORTAL ("UserPortal", ConnectionServicesType.USER_PORTAL),
	HealthCheck_Connection_CA ("Health Check", ConnectionServicesType.HEALTH_CHECK);
	
	String readableName;
	ConnectionServicesType connectionServicesType;
	
	private KeyStorePurpose (String readableName, ConnectionServicesType servicesType) {
		this.readableName = readableName;
		this.connectionServicesType = servicesType;
	}
	
	static public KeyStorePurpose getKeyStorePurpose (ConnectionServicesType connectionServicesType) {
		for (KeyStorePurpose keyStorePurpose : KeyStorePurpose.values()) {
			if (keyStorePurpose.connectionServicesType == connectionServicesType) {
				return keyStorePurpose;
			}
		}
		return null;
	}

}
