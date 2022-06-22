package com.doubleclue.dcem.saml.logic.enums;

import org.opensaml.xmlsec.signature.support.SignatureConstants;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DigestAlgorithmEnum {

	// DO NOT CHANGE THE ORDER OF THESE
	SHA1(SignatureConstants.ALGO_ID_DIGEST_SHA1, "SHA-1"),
	SHA224(SignatureConstants.ALGO_ID_DIGEST_SHA224, "SHA-224"),
	SHA256(SignatureConstants.ALGO_ID_DIGEST_SHA256, "SHA-256"),
	SHA384(SignatureConstants.ALGO_ID_DIGEST_SHA384, "SHA-384"),
	SHA512(SignatureConstants.ALGO_ID_DIGEST_SHA512, "SHA-512"),
	MD5(SignatureConstants.ALGO_ID_DIGEST_NOT_RECOMMENDED_MD5, "MD5"),
	RIPEMD160(SignatureConstants.ALGO_ID_DIGEST_RIPEMD160, "RIPEMD-160");

	private final String format;
	private final String displayName;

	private DigestAlgorithmEnum(String format, String displayName) {
		this.format = format;
		this.displayName = displayName;
	}

	public static DigestAlgorithmEnum getFromString(String format) throws IllegalArgumentException {
		for (DigestAlgorithmEnum id : values()) {
			if (id.format.equals(format)) {
				return id;
			}
		}
		throw new IllegalArgumentException("Format " + format + " unknown");
	}

	public String getFormat() {
		return format;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	@JsonValue
    public int toValue() {
        return ordinal();
    }
}