package com.doubleclue.oauth.oauth2.enums;

public enum OAuthError {

	INVALID_REQUEST("invalid_request"),
	INVALID_CLIENT("invalid_client"),
	INVALID_GRANT("invalid_grant"),
	UNAUTHORISED_CLIENT("unauthorized_client"),
	UNSUPPORTED_GRANT_TYPE("unsupported_grant_type"),
	UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type"),
	INSUFFICIENT_SCOPE("insufficient_scope"),
	INVALID_SCOPE("invalid_scope"),
	ACCESS_DENIED("access_denied"),
	SERVER_ERROR("server_error"),
	TEMPORARILY_UNAVAILABLE("temporarily_unavailable"),
	INVALID_TOKEN("invalid_token"),

	// OpenID
	INTERACTION_REQUIRED("interaction_required"),
	LOGIN_REQUIRED("login_required"),
	ACCOUNT_SELECTION_REQUIRED("account_selection_required"),
	CONSENT_REQUIRED("consent_required"),
	INVALID_REQUEST_URI("invalid_request_uri"),
	INVALID_REQUEST_OBJECT("invalid_request_object"),
	REQUEST_NOT_SUPPORTED("request_not_supported"),
	REQUEST_URI_NOT_SUPPORTED("request_uri_not_supported"),
	REGISTRATION_NOT_SUPPORTED("registration_not_supported");

	private final String value;

	private OAuthError(String value) {
		this.value = value;
	}

	public static OAuthError fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OAuthError b : OAuthError.values()) {
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

	public int getHttpStatusCode() {
		switch (this) {
		case INVALID_TOKEN:
			return 401;
		case INSUFFICIENT_SCOPE:
			return 403;
		default:
			return 400;
		}
	}
}
