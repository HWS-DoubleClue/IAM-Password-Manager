package com.doubleclue.oauth.openid.enums;

public enum OpenIdPrompt {

	NONE("none"),
	LOGIN("login"),
	CONSENT("consent"),
	SELECT_ACCOUNT("select_account");

	private final String value;

	private OpenIdPrompt(String value) {
		this.value = value;
	}

	public static OpenIdPrompt fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OpenIdPrompt b : OpenIdPrompt.values()) {
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
