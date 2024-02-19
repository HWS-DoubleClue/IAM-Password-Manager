package com.doubleclue.oauth.openid.enums;

public enum OpenIdScope {

	OPENID("openid"),
	PROFILE("profile"),
	EMAIL("email"),
	ADDRESS("address"),
	PHONE("phone");

	private final String value;

	private OpenIdScope(String value) {
		this.value = value;
	}

	public static OpenIdScope fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OpenIdScope b : OpenIdScope.values()) {
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

	public OpenIdClaim[] getReqestedClaims() {
		switch (this) {
		case PROFILE:
			return new OpenIdClaim[] { OpenIdClaim.FULL_NAME, OpenIdClaim.FAMILY_NAME, OpenIdClaim.GIVEN_NAME, OpenIdClaim.MIDDLE_NAME, OpenIdClaim.NICKNAME,
					OpenIdClaim.PREFERRED_USERNAME, OpenIdClaim.PROFILE_URL, OpenIdClaim.PICTURE_URL, OpenIdClaim.WEBSITE, OpenIdClaim.GENDER, OpenIdClaim.DOB,
					OpenIdClaim.TIME_ZONE, OpenIdClaim.LOCALE, OpenIdClaim.UPDATED_AT };
		case EMAIL:
			return new OpenIdClaim[] { OpenIdClaim.EMAIL, OpenIdClaim.EMAIL_VERIFIED };
		case ADDRESS:
			return new OpenIdClaim[] { OpenIdClaim.ADDRESS };
		case PHONE:
			return new OpenIdClaim[] { OpenIdClaim.PHONE_NUMBER, OpenIdClaim.PHONE_NUMBER_VERIFIED };
		default:
			return new OpenIdClaim[0];
		}
	}
}
