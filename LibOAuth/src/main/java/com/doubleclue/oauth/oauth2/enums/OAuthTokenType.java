package com.doubleclue.oauth.oauth2.enums;

public enum OAuthTokenType {

	BEARER("bearer"),
	MAC("mac");

	private final String value;

	private OAuthTokenType(String value) {
		this.value = value;
	}

	public static OAuthTokenType fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OAuthTokenType b : OAuthTokenType.values()) {
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
