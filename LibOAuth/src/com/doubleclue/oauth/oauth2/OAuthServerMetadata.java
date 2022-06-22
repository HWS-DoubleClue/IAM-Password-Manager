package com.doubleclue.oauth.oauth2;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import com.doubleclue.oauth.oauth2.enums.OAuthCodeChallengeMethod;
import com.doubleclue.oauth.oauth2.enums.OAuthGrantType;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseMode;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseType;
import com.doubleclue.oauth.oauth2.enums.OAuthServerMetadataSetting;
import com.doubleclue.oauth.oauth2.enums.OAuthTokenAuthMethod;
import com.doubleclue.oauth.utils.OAuthUtils;

import io.jsonwebtoken.SignatureAlgorithm;

public class OAuthServerMetadata {

	protected final Map<OAuthServerMetadataSetting, Object> settingMap = new HashMap<>();

	public OAuthServerMetadata() {
	}

	public OAuthServerMetadata(String json) {
		JSONObject obj = new JSONObject(json);
		for (OAuthServerMetadataSetting setting : OAuthServerMetadataSetting.values()) {
			if (obj.has(setting.toString())) {
				setSetting(setting, obj.get(setting.toString()));
			}
		}
	}

	public OAuthServerMetadata(Map<OAuthServerMetadataSetting, Object> settingMap) {
		for (OAuthServerMetadataSetting setting : settingMap.keySet()) {
			setSetting(setting, settingMap.get(setting));
		}
	}

	public String getJson() {
		return OAuthUtils.getJsonFromMaps(new Map[] { settingMap });
	}

	@Override
	public String toString() {
		return getJson();
	}

	public Map<OAuthServerMetadataSetting, Object> getSettingMap() {
		return settingMap;
	}

	public void setSetting(OAuthServerMetadataSetting key, Object value) {
		if (value != null) {
			settingMap.put(key, key.getExpectedClass().cast(value));
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getSetting(OAuthServerMetadataSetting key) {
		return (T) settingMap.get(key);
	}

	public boolean getBooleanSetting(OAuthServerMetadataSetting key) {
		Boolean param = getSetting(key);
		return param != null ? param : false;
	}

	protected void setAuthMethods(OAuthServerMetadataSetting setting, OAuthTokenAuthMethod[] authMethods) {
		if (authMethods != null) {
			String[] authMethodStrings = new String[authMethods.length];
			for (int i = 0; i < authMethods.length; i++) {
				authMethodStrings[i] = authMethods[i].toString();
			}
			setSetting(setting, authMethodStrings);
		}
	}

	protected OAuthTokenAuthMethod[] getAuthMethods(OAuthServerMetadataSetting setting) {
		String[] authMethodStrings = getSetting(setting);
		if (authMethodStrings != null) {
			OAuthTokenAuthMethod[] authMethods = new OAuthTokenAuthMethod[authMethodStrings.length];
			for (int i = 0; i < authMethodStrings.length; i++) {
				authMethods[i] = OAuthTokenAuthMethod.fromString(authMethodStrings[i]);
			}
			return authMethods;
		}
		return null;
	}

	protected void setSigningAlgs(OAuthServerMetadataSetting setting, SignatureAlgorithm[] signingAlgs) {
		if (signingAlgs != null) {
			String[] signingAlgStrings = new String[signingAlgs.length];
			for (int i = 0; i < signingAlgs.length; i++) {
				signingAlgStrings[i] = signingAlgs[i].getValue();
			}
			setSetting(setting, signingAlgStrings);
		}
	}

	protected SignatureAlgorithm[] getSigningAlgs(OAuthServerMetadataSetting setting) {
		String[] signingAlgStrings = getSetting(setting);
		if (signingAlgStrings != null) {
			SignatureAlgorithm[] signingAlgs = new SignatureAlgorithm[signingAlgStrings.length];
			for (int i = 0; i < signingAlgStrings.length; i++) {
				signingAlgs[i] = SignatureAlgorithm.forName(signingAlgStrings[i]);
			}
			return signingAlgs;
		}
		return null;
	}

	public void setIssuer(String issuer) {
		setSetting(OAuthServerMetadataSetting.ISSUER, issuer);
	}

	public String getIssuer() {
		return getSetting(OAuthServerMetadataSetting.ISSUER);
	}

	public void setAuthEndpoint(String endpoint) {
		setSetting(OAuthServerMetadataSetting.AUTH_ENDPOINT, endpoint);
	}

	public String getAuthEndpoint() {
		return getSetting(OAuthServerMetadataSetting.AUTH_ENDPOINT);
	}

	public void setTokenEndpoint(String endpoint, OAuthTokenAuthMethod[] authMethods, SignatureAlgorithm[] signingAlgs) {
		setSetting(OAuthServerMetadataSetting.TOKEN_ENDPOINT, endpoint);
		setAuthMethods(OAuthServerMetadataSetting.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED, authMethods);
		setSigningAlgs(OAuthServerMetadataSetting.TOKEN_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED, signingAlgs);
	}

	public String getTokenEndpoint() {
		return getSetting(OAuthServerMetadataSetting.TOKEN_ENDPOINT);
	}

	public OAuthTokenAuthMethod[] getTokenEndpointAuthMethods() {
		return getAuthMethods(OAuthServerMetadataSetting.TOKEN_ENDPOINT_AUTH_METHODS_SUPPORTED);
	}

	public SignatureAlgorithm[] getTokenEndpointSigningAlgs() {
		return getSigningAlgs(OAuthServerMetadataSetting.TOKEN_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED);
	}

	public void setJwksUri(String jwks) {
		setSetting(OAuthServerMetadataSetting.JWKS_URI, jwks);
	}

	public String getJwksUri() {
		return getSetting(OAuthServerMetadataSetting.JWKS_URI);
	}

	public void setRegistrationEndpoint(String endpoint) {
		setSetting(OAuthServerMetadataSetting.REGISTRATION_ENDPOINT, endpoint);
	}

	public String getRegistrationEndpoint() {
		return getSetting(OAuthServerMetadataSetting.REGISTRATION_ENDPOINT);
	}

	public void setSupportedScopes(String[] scopes) {
		setSetting(OAuthServerMetadataSetting.SCOPES_SUPPORTED, scopes);
	}

	public String[] getSupportedScopes() {
		return getSetting(OAuthServerMetadataSetting.SCOPES_SUPPORTED);
	}

	public void setSupportedResponseTypes(OAuthResponseType[] responseTypes) {
		if (responseTypes != null) {
			String[] responseTypeStrings = new String[responseTypes.length];
			for (int i = 0; i < responseTypes.length; i++) {
				responseTypeStrings[i] = responseTypes[i].toString();
			}
			setSetting(OAuthServerMetadataSetting.RESPONSE_TYPES_SUPPORTED, responseTypeStrings);
		}
	}

	public OAuthResponseType[] getSupportedResponseTypes() {
		String[] responseTypeStrings = getSetting(OAuthServerMetadataSetting.RESPONSE_TYPES_SUPPORTED);
		if (responseTypeStrings != null) {
			OAuthResponseType[] responseTypes = new OAuthResponseType[responseTypeStrings.length];
			for (int i = 0; i < responseTypeStrings.length; i++) {
				responseTypes[i] = OAuthResponseType.fromString(responseTypeStrings[i]);
			}
			return responseTypes;
		}
		return null;
	}

	public void setSupportedResponseModes(OAuthResponseMode[] responseModes) {
		if (responseModes != null) {
			String[] responseModeStrings = new String[responseModes.length];
			for (int i = 0; i < responseModes.length; i++) {
				responseModeStrings[i] = responseModes[i].toString();
			}
			setSetting(OAuthServerMetadataSetting.RESPONSE_MODES_SUPPORTED, responseModeStrings);
		}
	}

	public OAuthResponseMode[] getSupportedResponseModes() {
		String[] responseModeStrings = getSetting(OAuthServerMetadataSetting.RESPONSE_MODES_SUPPORTED);
		if (responseModeStrings != null) {
			OAuthResponseMode[] responseModes = new OAuthResponseMode[responseModeStrings.length];
			for (int i = 0; i < responseModeStrings.length; i++) {
				responseModes[i] = OAuthResponseMode.fromString(responseModeStrings[i]);
			}
			return responseModes;
		}
		return null;
	}

	public void setSupportedGrantTypes(OAuthGrantType[] grantTypes) {
		if (grantTypes != null) {
			String[] grantTypeStrings = new String[grantTypes.length];
			for (int i = 0; i < grantTypes.length; i++) {
				grantTypeStrings[i] = grantTypes[i].toString();
			}
			setSetting(OAuthServerMetadataSetting.GRANT_TYPES_SUPPORTED, grantTypeStrings);
		}
	}

	public OAuthGrantType[] getSupportedGrantTypes() {
		String[] grantTypeStrings = getSetting(OAuthServerMetadataSetting.GRANT_TYPES_SUPPORTED);
		if (grantTypeStrings != null) {
			OAuthGrantType[] grantTypes = new OAuthGrantType[grantTypeStrings.length];
			for (int i = 0; i < grantTypeStrings.length; i++) {
				grantTypes[i] = OAuthGrantType.fromString(grantTypeStrings[i]);
			}
			return grantTypes;
		}
		return null;
	}

	public void setServiceDocumentation(String serviceDocumentation) {
		setSetting(OAuthServerMetadataSetting.SERVICE_DOCUMENTATION, serviceDocumentation);
	}

	public String getServiceDocumentation() {
		return getSetting(OAuthServerMetadataSetting.SERVICE_DOCUMENTATION);
	}

	public void setSupportedUiLocales(Locale[] locales) {
		if (locales != null) {
			String[] localeStrings = new String[locales.length];
			for (int i = 0; i < locales.length; i++) {
				localeStrings[i] = locales[i].getLanguage();
			}
			setSetting(OAuthServerMetadataSetting.UI_LOCALES_SUPPORTED, localeStrings);
		}
	}

	public Locale[] getSupportedUiLocales() {
		String[] localeStrings = getSetting(OAuthServerMetadataSetting.UI_LOCALES_SUPPORTED);
		if (localeStrings != null) {
			Locale[] locales = new Locale[localeStrings.length];
			for (int i = 0; i < localeStrings.length; i++) {
				locales[i] = new Locale(localeStrings[i]);
			}
			return locales;
		}
		return null;
	}

	public void setOpPolicyUri(String opPolicyUri) {
		setSetting(OAuthServerMetadataSetting.OP_POLICY_URI, opPolicyUri);
	}

	public String getOpPolicyUri() {
		return getSetting(OAuthServerMetadataSetting.OP_POLICY_URI);
	}

	public void setOpTosUri(String opTosUri) {
		setSetting(OAuthServerMetadataSetting.OP_TOS_URI, opTosUri);
	}

	public String getOpTosUri() {
		return getSetting(OAuthServerMetadataSetting.OP_TOS_URI);
	}

	public void setRevocationEndpoint(String endpoint, OAuthTokenAuthMethod[] authMethods, SignatureAlgorithm[] signingAlgs) {
		setSetting(OAuthServerMetadataSetting.REVOCATION_ENDPOINT, endpoint);
		setAuthMethods(OAuthServerMetadataSetting.REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED, authMethods);
		setSigningAlgs(OAuthServerMetadataSetting.REVOCATION_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED, signingAlgs);
	}

	public String getRevocationEndpoint() {
		return getSetting(OAuthServerMetadataSetting.REVOCATION_ENDPOINT);
	}

	public OAuthTokenAuthMethod[] getRevocationEndpointAuthMethods() {
		return getAuthMethods(OAuthServerMetadataSetting.REVOCATION_ENDPOINT_AUTH_METHODS_SUPPORTED);
	}

	public SignatureAlgorithm[] getRevocationEndpointSigningAlgs() {
		return getSigningAlgs(OAuthServerMetadataSetting.REVOCATION_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED);
	}

	public void setIntrospectionEndpoint(String endpoint, OAuthTokenAuthMethod[] authMethods, SignatureAlgorithm[] signingAlgs) {
		setSetting(OAuthServerMetadataSetting.INTROSPECTION_ENDPOINT, endpoint);
		setAuthMethods(OAuthServerMetadataSetting.INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED, authMethods);
		setSigningAlgs(OAuthServerMetadataSetting.INTROSPECTION_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED, signingAlgs);
	}

	public String getIntrospectionEndpoint() {
		return getSetting(OAuthServerMetadataSetting.INTROSPECTION_ENDPOINT);
	}

	public OAuthTokenAuthMethod[] getIntrospectionEndpointAuthMethods() {
		return getAuthMethods(OAuthServerMetadataSetting.INTROSPECTION_ENDPOINT_AUTH_METHODS_SUPPORTED);
	}

	public SignatureAlgorithm[] getIntrospectionEndpointSigningAlgs() {
		return getSigningAlgs(OAuthServerMetadataSetting.INTROSPECTION_ENDPOINT_AUTH_SIGNING_ALG_VALUES_SUPPORTED);
	}

	public void setSupportedCodeChallengeMethods(OAuthCodeChallengeMethod[] methods) {
		if (methods != null) {
			String[] methodStrings = new String[methods.length];
			for (int i = 0; i < methods.length; i++) {
				methodStrings[i] = methods[i].toString();
			}
			setSetting(OAuthServerMetadataSetting.CODE_CHALLENGE_METHODS_SUPPORTED, methodStrings);
		}
	}

	public OAuthCodeChallengeMethod[] getSupportedCodeChallengeMethods() {
		String[] methodStrings = getSetting(OAuthServerMetadataSetting.CODE_CHALLENGE_METHODS_SUPPORTED);
		if (methodStrings != null) {
			OAuthCodeChallengeMethod[] methods = new OAuthCodeChallengeMethod[methodStrings.length];
			for (int i = 0; i < methodStrings.length; i++) {
				methods[i] = OAuthCodeChallengeMethod.fromString(methodStrings[i]);
			}
			return methods;
		}
		return null;
	}

	public void setSignedMetadata(String signedMetadata) {
		setSetting(OAuthServerMetadataSetting.SIGNED_METADATA, signedMetadata);
	}

	public String getSignedMetadata() {
		return getSetting(OAuthServerMetadataSetting.SIGNED_METADATA);
	}
}
