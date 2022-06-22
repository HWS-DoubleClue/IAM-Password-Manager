package com.doubleclue.oauth.jwk.enums;

public enum JwkParam {

	KEY_ID("kid"),
	KEY_TYPE("kty"),
	KEY_OPS("key_ops"),
	USE("use"),
	MODULUS("n"),
	EXPONENT("e"),
	X509_URL("x5u"),
	X509_CHAIN("x5c"),
	X509_SHA1_THUMBPRINT("x5t"),
	X509_SHA256_THUMBPRINT("x5t#S256");

	private final String value;

	private JwkParam(String value) {
		this.value = value;
	}

	public static JwkParam fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (JwkParam b : JwkParam.values()) {
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
		case X509_CHAIN:
		case KEY_OPS:
			return String[].class;
		default:
			return String.class;
		}
	}
}
