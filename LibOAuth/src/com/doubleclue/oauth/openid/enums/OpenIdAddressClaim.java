package com.doubleclue.oauth.openid.enums;

public enum OpenIdAddressClaim {

	FORMATTED("formatted"),
	STREET_ADDRESS("street_address"),
	LOCALITY("locality"),
	REGION("region"),
	POSTCODE("postal_code"),
	COUNTRY("country");

	private final String value;

	private OpenIdAddressClaim(String value) {
		this.value = value;
	}

	public static OpenIdAddressClaim fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OpenIdAddressClaim b : OpenIdAddressClaim.values()) {
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
