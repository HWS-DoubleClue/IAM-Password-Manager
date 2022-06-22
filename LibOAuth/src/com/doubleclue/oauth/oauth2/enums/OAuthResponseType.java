package com.doubleclue.oauth.oauth2.enums;

public enum OAuthResponseType {

	// RFC6749
	AUTH_CODE("code"),
	TOKEN("token"),

	// OpenID Connect
	ID_TOKEN("id_token");

	private final String value;

	private OAuthResponseType(String value) {
		this.value = value;
	}

	public static OAuthResponseType fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OAuthResponseType b : OAuthResponseType.values()) {
				if (b.value.equalsIgnoreCase(text)) {
					return b;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return value;
	}
}
