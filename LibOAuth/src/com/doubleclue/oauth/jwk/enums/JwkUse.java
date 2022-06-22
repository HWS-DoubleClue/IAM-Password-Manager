package com.doubleclue.oauth.jwk.enums;

public enum JwkUse {

	SIGNATURE("sig"),
	ENCRYPTION("enc");

	private final String value;

	private JwkUse(String value) {
		this.value = value;
	}

	public static JwkUse fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (JwkUse b : JwkUse.values()) {
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
