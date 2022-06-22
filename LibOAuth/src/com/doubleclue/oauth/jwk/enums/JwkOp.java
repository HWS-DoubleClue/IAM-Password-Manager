package com.doubleclue.oauth.jwk.enums;

public enum JwkOp {

	SIGN("sign"),
	VERIFY("verify"),
	ENCRYPT("encrypt"),
	DECRYPT("decrypt"),
	WRAP_KEY("wrapKey"),
	UNWRAP_KEY("unwrapKey"),
	DERIVE_KEY("deriveKey"),
	DERIVE_BITS("deriveBits");

	private final String value;

	private JwkOp(String value) {
		this.value = value;
	}

	public static JwkOp fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (JwkOp b : JwkOp.values()) {
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
