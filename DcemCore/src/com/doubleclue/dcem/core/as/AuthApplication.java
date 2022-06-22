package com.doubleclue.dcem.core.as;

import com.doubleclue.dcem.core.gui.EnumStringAnnotation;

@EnumStringAnnotation  // This means taht this Enum is stored in DB as a String
public enum AuthApplication {

	/*
	 * DO NOT CHANGE THE ENUM NAMES. THESE ARE STORED IN DB
	 */
	WebServices("Web-Services", true, false),
	RADIUS("RADIUS", true, false),
	SAML("SAML", true, true),
	OAUTH("OAuth/OpenID", true, true),
	AuthGateway("Auth Connector", true, false),
	DCEM("DCEM-Management", true, true),
	OTP("OTP", false, false),
	SECURE_APP("SecureApp", false, false), 
	USER_PORTAL ("User-Portal", true, true),
	PERFORMANCE_DECK ("Performance-Deck", false, false);

	String name;
	boolean withPolicies;
	boolean shareSession;

	private AuthApplication(String name, boolean withPolicies, boolean shareSession) {
		this.name = name;
		this.withPolicies = withPolicies;
		this.shareSession = shareSession;
	}

	public String toString() {
		return name;
	}

	public boolean isWithPolicies() {
		return withPolicies;
	}
	
	public boolean isShareSession() {
		return shareSession;
	}

}
