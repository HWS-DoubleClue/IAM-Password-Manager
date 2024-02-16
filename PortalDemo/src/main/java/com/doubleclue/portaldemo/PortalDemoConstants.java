package com.doubleclue.portaldemo;

public class PortalDemoConstants {

	public static final String PD_RESOURCE = "com.doubleclue.portaldemo.resources.Messages";

	// FIDO
	public static final String FIDO_PARAM_JSON = "json";
	public static final String FIDO_ERROR_ABORTED_CHROME = "The operation either timed out or was not allowed. See: https://w3c.github.io/webauthn/#sec-assertion-privacy.";
	public static final String FIDO_ERROR_ABORTED_FIREFOX = "The operation was aborted. ";
	public static final String FIDO_ERROR_NO_AUTHENTICATORS_CHROME = "Resident credentials or empty 'allowCredentials' lists are not supported at this time.";
	public static final String FIDO_ERROR_NOT_REGISTERED_CHROME = "The user attempted to use an authenticator that recognized none of the provided credentials.";
	public static final String FIDO_ERROR_NOT_REGISTERED_FIREFOX = "An attempt was made to use an object that is not, or is no longer, usable";
	
	public final static int POLL_INTERVAL_MILLI_SECONDS = 4000;
	public final static int WAIT_INTERVAL_MILLI_SECONDS = 3500;
}
