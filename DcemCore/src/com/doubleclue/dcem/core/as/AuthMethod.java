package com.doubleclue.dcem.core.as;

/**
 * Gets or Sets asApiAuthMethod
 */
public enum AuthMethod {

	PASSWORD("Password", "pwd"),
	SMS("SMS Passcode", "sms"),
	VOICE_MESSAGE("Voice Message", "voice"),
	HARDWARE_TOKEN("OTP Token", "otp"),
	DOUBLECLUE_PASSCODE("DoubleClue Passcode", "motp"),
	PUSH_APPROVAL("Push Approval", "push"),
	QRCODE_APPROVAL("Qr-Code Approval", "qrcode"),
	SESSION_RECONNECT(null, null),
	FIDO_U2F("FIDO Authentication", "fido");
//	WINDOWS_SSO (null, null),;

	/*
	 * if value is null, then this auth method cannot be selected by user
	 */
	private String value;
	private String abbreviation;
	// private ThriftAuthMethod thriftAuthMethod;

	public String getValue() {
		return value;
	}

	public void setAbbriviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	AuthMethod(String value, String abbreviation) {
		this.value = value;
		this.abbreviation = abbreviation;
	}

	@Override
	public String toString() {
		return value;
	}

	public static AuthMethod fromValue(String value) {
		for (AuthMethod apiAuthMethod : AuthMethod.values()) {
			if (apiAuthMethod.name().equals(value)) {
				return apiAuthMethod;
			}
		}
		return null;
	}

	public static AuthMethod fromAbbr(String value) {
		for (AuthMethod apiAuthMethod : AuthMethod.values()) {
			if (apiAuthMethod.abbreviation != null && apiAuthMethod.abbreviation.equals(value)) {
				return apiAuthMethod;
			}
		}
		return null;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

}
