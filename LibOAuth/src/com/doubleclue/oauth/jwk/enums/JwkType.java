package com.doubleclue.oauth.jwk.enums;

public enum JwkType {

	EC("EC"),
	RSA("RSA"),
	OCTET("oct");

	private final String value;

	private JwkType(String value) {
		this.value = value;
	}

	public static JwkType fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (JwkType b : JwkType.values()) {
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
