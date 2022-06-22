package com.doubleclue.oauth.oauth2.enums;

public enum OAuthCodeChallengeMethod {

	// RFC7636
	PLAIN("plain"),
	SHA_256("s256");

	private final String value;

	private OAuthCodeChallengeMethod(String value) {
		this.value = value;
	}

	public static OAuthCodeChallengeMethod fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OAuthCodeChallengeMethod b : OAuthCodeChallengeMethod.values()) {
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
