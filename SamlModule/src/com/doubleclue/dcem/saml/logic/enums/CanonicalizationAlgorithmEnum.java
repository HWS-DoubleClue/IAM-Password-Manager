package com.doubleclue.dcem.saml.logic.enums;

import org.opensaml.xmlsec.signature.support.SignatureConstants;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CanonicalizationAlgorithmEnum {

	// DO NOT CHANGE THE ORDER OF THESE
	INCL_WITH_COMMENTS(SignatureConstants.ALGO_ID_C14N_WITH_COMMENTS, "Inclusive (1.0) with Comments"),
	INCL_OMIT_COMMENTS(SignatureConstants.ALGO_ID_C14N_OMIT_COMMENTS, "Inclusive (1.0) without Comments"),
	INCL_11_WITH_COMMENTS(SignatureConstants.ALGO_ID_C14N11_WITH_COMMENTS, "Inclusive (1.1) with Comments"),
	INCL_11_OMIT_COMMENTS(SignatureConstants.ALGO_ID_C14N11_OMIT_COMMENTS, "Inclusive (1.1) without Comments"),
	EXCL_WITH_COMMENTS(SignatureConstants.ALGO_ID_C14N_EXCL_WITH_COMMENTS, "Exclusive with Comments"),
	EXCL_OMIT_COMMENTS(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS, "Exclusive without Comments");

	private final String format;
	private final String displayName;

	private CanonicalizationAlgorithmEnum(String format, String displayName) {
		this.format = format;
		this.displayName = displayName;
	}

	public static CanonicalizationAlgorithmEnum getFromString(String format) throws IllegalArgumentException {
		for (CanonicalizationAlgorithmEnum id : values()) {
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