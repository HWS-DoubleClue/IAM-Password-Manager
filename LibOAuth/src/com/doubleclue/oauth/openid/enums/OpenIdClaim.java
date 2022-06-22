package com.doubleclue.oauth.openid.enums;

public enum OpenIdClaim {

	SUBJECT("sub"),
	FULL_NAME("name"),
	GIVEN_NAME("given_name"),
	FAMILY_NAME("family_name"),
	MIDDLE_NAME("middle_name"),
	NICKNAME("nickname"),
	PREFERRED_USERNAME("preferred_username"),
	PROFILE_URL("profile"),
	PICTURE_URL("picture"),
	WEBSITE("website"),
	EMAIL("email"),
	EMAIL_VERIFIED("email_verified"),
	GENDER("gender"),
	DOB("birthdate"),
	TIME_ZONE("zoneinfo"),
	LOCALE("locale"),
	PHONE_NUMBER("phone_number"),
	PHONE_NUMBER_VERIFIED("phone_number_verified"),
	ADDRESS("address"),
	UPDATED_AT("updated_at"),
	AUTH_TIME("auth_time"),
	ACR("acr"),

	// Hybrid Flow
	NONCE("nonce"),
	ACCESS_TOKEN_HASH("at_hash"),
	AUTH_CODE_HASH("c_hash");

	private final String value;

	private OpenIdClaim(String value) {
		this.value = value;
	}

	public static OpenIdClaim fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OpenIdClaim b : OpenIdClaim.values()) {
				if (b.value.equalsIgnoreCase(text)) {
					return b;
				}
			}
		}
		return null;
	}

	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return getValue();
	}

	public Class<?> getExpectedClass() {
		switch (this) {
		case EMAIL_VERIFIED:
		case PHONE_NUMBER_VERIFIED:
			return Boolean.class;
		case UPDATED_AT:
		case AUTH_TIME:
			return Long.class;
		default:
			return String.class;
		}
	}
}
