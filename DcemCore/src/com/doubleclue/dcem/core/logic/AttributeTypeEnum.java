package com.doubleclue.dcem.core.logic;

// Author : Kenneth Ellul
// NB : DO NOT change the order of these ENUMS since it will affect data stored in the database

public enum AttributeTypeEnum {

	LOGIN_ID("User Login ID", false),
	DISPLAY_NAME("User Display Name", false),
	ACCOUNT_NAME("User Account Name", false),
	EMAIL("User Email", false),
	UPN("User Principal Name", false),
	TELEPHONE("User Telephone", false),
	MOBILE("User Mobile", false),
	LOCALE("User Locale", false),
	CLOUD_SAFE_USER("User CloudSafe", true),
	USER_INPUT("PasswordSafe Entry-Input", true),
	PASSWORD ("User Password", false),
	STATIC_TEXT ("Static Text", true),
	AUTHENTICATOR_PASSCODE ("Authenticator Passcode", false)
	// #if COMMUNITY_EDITION == false
	,GROUPS("Groups", true),
	DOMAIN_ATTRIBUTE("Domain Attribute", true),
	POLICY ("Policy Name", false),
	AD_OBJECT_GUID ("AD ObjectGUID", false)
	// #endif
;
	private String displayName;
	private boolean valueRequired;

	private AttributeTypeEnum(String name, boolean valueRequired) {
		this.displayName = name;
		this.valueRequired = valueRequired;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public boolean isValueRequired() {
		return valueRequired;
	}
}

