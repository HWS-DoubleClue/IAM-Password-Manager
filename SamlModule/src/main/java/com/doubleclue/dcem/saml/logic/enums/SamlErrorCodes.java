package com.doubleclue.dcem.saml.logic.enums;

public enum SamlErrorCodes {

	INCORRECT_URL(1),
	REQUEST_OR_ARTIFACT_MISSING(2),
	UNSUPPORTED_ENCODING(3),
	INVALID_REQUEST(4),
	UNKNOWN_SP(5),
	REQUEST_MISSING(6),
	REQUEST_PARSE_ERROR(7),
	UNKNOWN_REQUEST_TYPE(8);

	int errorCode;

	private SamlErrorCodes(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}