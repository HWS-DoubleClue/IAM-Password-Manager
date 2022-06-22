package com.doubleclue.oauth.oauth2.enums;

public enum OAuthGrantType {

	PASSWORD("password"),
	AUTH_CODE("authorization_code"),
	CLIENT_CREDENTIALS("client_credentials"),
	REFRESH_TOKEN("refresh_token");

	private final String value;

	private OAuthGrantType(String value) {
		this.value = value;
	}

	public static OAuthGrantType fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OAuthGrantType b : OAuthGrantType.values()) {
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
