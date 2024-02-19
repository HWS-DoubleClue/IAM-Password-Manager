package com.doubleclue.oauth.oauth2.enums;

public enum OAuthResponseMode {

	QUERY("query"),
	FRAGMENT("fragment"),
	FORM_POST("form_post");

	private final String value;

	private OAuthResponseMode(String value) {
		this.value = value;
	}

	public static OAuthResponseMode fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OAuthResponseMode b : OAuthResponseMode.values()) {
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
