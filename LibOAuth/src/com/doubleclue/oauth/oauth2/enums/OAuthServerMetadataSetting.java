package com.doubleclue.oauth.oauth2.enums;

public enum OAuthServerMetadataSetting {

	// RFC8414
	ISSUER("issuer"),
	AUTH_ENDPOINT("authorization_endpoint"),
	TOKEN_ENDPOINT("token_endpoint"),
	TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED("token_endpoint_auth_methods_supported"),
	TOKEN_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED("token_endpoint_auth_signing_alg_values_supported"),
	JWKS_URI("jwks_uri"),
	REGISTRATION_ENDPOINT("registration_endpoint"),
	SCOPES_SUPPORTED("scopes_supported"),
	RESPONSE_TYPES_SUPPORTED("response_types_supported"),
	RESPONSE_MODES_SUPPORTED("response_modes_supported"),
	GRANT_TYPES_SUPPORTED("grant_types_supported"),
	SERVICE_DOCUMENTATION("service_documentation"),
	UI_LOCALES_SUPPORTED("ui_locales_supported"),
	OP_POLICY_URI("op_policy_uri"),
	OP_TOS_URI("op_tos_uri"),
	REVOCATION_ENDPOINT("revocation_endpoint"),
	REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED("revocation_endpoint_auth_methods_supported"),
	REVOCATION_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED("revocation_endpoint_auth_signing_alg_values_supported"),
	INTROSPECTION_ENDPOINT("introspection_endpoint"),
	INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED("introspection_endpoint_auth_methods_supported"),
	INTROSPECTION_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED("introspection_endpoint_auth_signing_alg_values_supported"),
	CODE_CHALLENGE_METHODS_SUPPORTED("code_challenge_methods_supported"),
	SIGNED_METADATA("signed_metadata"),

	// OpenID Connect
	USERINFO_ENDPOINT("userinfo_endpoint"),
	USERINFO_SIGNING_ALG_VALUES_SUPPORTED("userinfo_signing_alg_values_supported"),
	USERINFO_ENCRYPTION_ALG_VALUES_SUPPORTED("userinfo_encryption_alg_values_supported"),
	USERINFO_ENCRYPTION_ENC_VALUES_SUPPORTED("userinfo_encryption_enc_values_supported"),
	ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED("id_token_signing_alg_values_supported"),
	ID_TOKEN_ENCRYPTION_ALG_VALUES_SUPPORTED("id_token_encryption_alg_values_supported"),
	ID_TOKEN_ENCRYPTION_ENC_VALUES_SUPPORTED("id_token_encryption_enc_values_supported"),
	DISPLAY_VALUES_SUPPORTED("display_values_supported"),
	CLAIM_TYPES_SUPPORTED("claim_types_supported"),
	CLAIMS_SUPPORTED("claims_supported"),
	CLAIMS_LOCALES_SUPPORTED("claims_locales_supported"),
	CLAIMS_PARAMETER_SUPPORTED("claims_parameter_supported"),
	REQUEST_PARAMETER_SUPPORTED("request_parameter_supported"),
	REQUEST_URI_PARAMETER_SUPPORTED("request_uri_parameter_supported"),
	REQUIRE_REQUEST_URI_REGISTRATION("require_request_uri_registration");

	private final String value;

	private OAuthServerMetadataSetting(String value) {
		this.value = value;
	}

	public static OAuthServerMetadataSetting fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OAuthServerMetadataSetting b : OAuthServerMetadataSetting.values()) {
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
		case TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED:
		case TOKEN_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED:
		case SCOPES_SUPPORTED:
		case RESPONSE_TYPES_SUPPORTED:
		case RESPONSE_MODES_SUPPORTED:
		case GRANT_TYPES_SUPPORTED:
		case UI_LOCALES_SUPPORTED:
		case REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED:
		case REVOCATION_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED:
		case INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED:
		case INTROSPECTION_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED:
		case CODE_CHALLENGE_METHODS_SUPPORTED:
		case USERINFO_SIGNING_ALG_VALUES_SUPPORTED:
		case USERINFO_ENCRYPTION_ALG_VALUES_SUPPORTED:
		case USERINFO_ENCRYPTION_ENC_VALUES_SUPPORTED:
		case ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED:
		case ID_TOKEN_ENCRYPTION_ALG_VALUES_SUPPORTED:
		case ID_TOKEN_ENCRYPTION_ENC_VALUES_SUPPORTED:
		case DISPLAY_VALUES_SUPPORTED:
		case CLAIM_TYPES_SUPPORTED:
		case CLAIMS_SUPPORTED:
		case CLAIMS_LOCALES_SUPPORTED:
			return String[].class;
		case CLAIMS_PARAMETER_SUPPORTED:
		case REQUEST_PARAMETER_SUPPORTED:
		case REQUEST_URI_PARAMETER_SUPPORTED:
		case REQUIRE_REQUEST_URI_REGISTRATION:
			return Boolean.class;
		default:
			return String.class;
		}
	}
}
