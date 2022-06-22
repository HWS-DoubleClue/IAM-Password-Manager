package com.doubleclue.dcem.saml.logic.enums;

import org.opensaml.xmlsec.signature.support.SignatureConstants;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SignatureAlgorithmEnum {

	// DO NOT CHANGE THE ORDER OF THESE
	DSA_SHA1(SignatureConstants.ALGO_ID_SIGNATURE_DSA_SHA1, "DSA with SHA-1"),
	DSA_SHA256(SignatureConstants.ALGO_ID_SIGNATURE_DSA_SHA256, "DSA with SHA-256"),
	ECDSA_SHA1(SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA1, "ECDSA with SHA-1"),
	ECDSA_SHA224(SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA224, "ECDSA with SHA-224"),
	ECDSA_SHA256(SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256, "ECDSA with SHA-256"),
	ECDSA_SHA384(SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA384, "ECDSA with SHA-384"),
	ECDSA_SHA512(SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA512, "ECDSA with SHA-512"),
	RSA_SHA1(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA1, "RSA with SHA-1"),
	RSA_SHA224(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA224, "RSA with SHA-224"),
	RSA_SHA256(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256, "RSA with SHA-256"),
	RSA_SHA384(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA384, "RSA with SHA-384"),
	RSA_SHA512(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA512, "RSA with SHA-512"),
	RSA_MD5(SignatureConstants.ALGO_ID_SIGNATURE_NOT_RECOMMENDED_RSA_MD5, "RSA with MD5"),
	RSA_RIPEMD160(SignatureConstants.ALGO_ID_SIGNATURE_RSA_RIPEMD160, "RSA with RIPEMD-160");

	private final String format;
	private final String displayName;

	private SignatureAlgorithmEnum(String format, String displayName) {
		this.format = format;
		this.displayName = displayName;
	}

	public static SignatureAlgorithmEnum getFromString(String format) throws IllegalArgumentException {
		for (SignatureAlgorithmEnum id : values()) {
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
