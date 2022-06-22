package com.doubleclue.oauth.oauth2.enums;

public enum OAuthTokenAuthMethod {

	// RFC7591
	NONE("none"),
	CLIENT_SECRET_POST("client_secret_post"),
	CLIENT_SECRET_BASIC("client_secret_basic"),

	// OpenID Connect
	CLIENT_SECRET_JWT("client_secret_jwt"),
	PRIVATE_KEY_JWT("private_key_jwt");

	private final String value;

	private OAuthTokenAuthMethod(String value) {
		this.value = value;
	}

	public static OAuthTokenAuthMethod fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OAuthTokenAuthMethod b : OAuthTokenAuthMethod.values()) {
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
