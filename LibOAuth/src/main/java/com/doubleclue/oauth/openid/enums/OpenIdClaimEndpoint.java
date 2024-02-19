package com.doubleclue.oauth.openid.enums;

public enum OpenIdClaimEndpoint {

	USER_INFO("userinfo"),
	ID_TOKEN("id_token");

	private final String value;

	private OpenIdClaimEndpoint(String value) {
		this.value = value;
	}

	public static OpenIdClaimEndpoint fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OpenIdClaimEndpoint b : OpenIdClaimEndpoint.values()) {
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
