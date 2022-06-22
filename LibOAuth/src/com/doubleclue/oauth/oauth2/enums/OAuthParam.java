package com.doubleclue.oauth.oauth2.enums;

public enum OAuthParam {

	// RFC6749
	GRANT_TYPE("grant_type"),
	RESPONSE_TYPE("response_type"),
	CLIENT_ID("client_id"),
	CLIENT_SECRET("client_secret"),
	USERNAME("username"),
	PASSWORD("password"),
	AUTH_CODE("code"),
	REDIRECT_URI("redirect_uri"),
	ACCESS_TOKEN("access_token"),
	REFRESH_TOKEN("refresh_token"),
	TOKEN_TYPE("token_type"),
	EXPIRES_IN("expires_in"),
	STATE("state"),
	SCOPE("scope"),
	ERROR("error"),
	ERROR_DESCRIPTION("error_description"),
	ERROR_URI("error_uri"),

	// RFC7521
	ASSERTION("assertion"),
	CLIENT_ASSERTION("client_assertion"),
	CLIENT_ASSERTION_TYPE("client_assertion_type"),

	// RFC7636
	CODE_CHALLENGE("code_challenge"),
	CODE_CHALLENGE_METHOD("code_challenge_method"),
	CODE_VERIFIER("code_verifier"),

	// Unnumbered OAuth 2.0 Specifications
	RESPONSE_MODE("response_mode"),

	// OpenID Connect
	NONCE("nonce"),
	DISPLAY("display"),
	PROMPT("prompt"),
	MAX_AGE("max_age"),
	UI_LOCALES("ui_locales"),
	ID_TOKEN_HINT("id_token_hint"),
	LOGIN_HINT("login_hint"),
	ACR_VALUES("acr_values"),
	ID_TOKEN("id_token"),
	CLAIMS("claims"),
	REQUEST("request"),
	REQUEST_URI("request_uri");

	private final String value;

	private OAuthParam(String value) {
		this.value = value;
	}

	public static OAuthParam fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OAuthParam b : OAuthParam.values()) {
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
		case EXPIRES_IN:
		case MAX_AGE:
			return Integer.class;
		default:
			return String.class;
		}
	}
}
