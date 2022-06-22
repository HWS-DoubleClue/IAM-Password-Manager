package com.doubleclue.oauth.openid.enums;

public enum OpenIdClaimRequestInfoEnum {

	ESSENTIAL("essential"),
	VALUE("value"),
	VALUES("values");

	private final String value;

	private OpenIdClaimRequestInfoEnum(String value) {
		this.value = value;
	}

	public static OpenIdClaimRequestInfoEnum fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OpenIdClaimRequestInfoEnum b : OpenIdClaimRequestInfoEnum.values()) {
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

	public Class<?> getExpectedClass() {
		switch (this) {
		case ESSENTIAL:
			return Boolean.class;
		case VALUES:
			return String[].class;
		default:
			return String.class;
		}
	}
}
