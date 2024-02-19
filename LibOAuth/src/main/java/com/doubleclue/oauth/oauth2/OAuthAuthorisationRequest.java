package com.doubleclue.oauth.oauth2;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.enums.OAuthCodeChallengeMethod;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseMode;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseType;

@SuppressWarnings("serial")
public class OAuthAuthorisationRequest extends OAuthRequest {

	public OAuthAuthorisationRequest() {
	}

	public OAuthAuthorisationRequest(HttpServletRequest request) {
		super(request);
	}

	public OAuthAuthorisationRequest(OAuthRequest request) {
		paramMap.putAll(request.getParamMap());
	}

	public OAuthAuthorisationRequest(String json) {
		super(json);
	}

	protected OAuthAuthorisationRequest(OAuthResponseType[] responseTypes, String clientId, String redirectUri, String state,
			OAuthResponseMode responseMode) {
		setResponseTypes(responseTypes);
		setClientId(clientId);
		setRedirectUri(redirectUri);
		setState(state);
		setResponseMode(responseMode);
	}

	public OAuthAuthorisationRequest(OAuthResponseType[] responseTypes, String clientId, String redirectUri, String[] scopes, String state,
			OAuthResponseMode responseMode) {
		this(responseTypes, clientId, redirectUri, state, responseMode);
		setScopes(scopes);
	}

	public OAuthAuthorisationRequest(OAuthResponseType responseType, String clientId, String redirectUri, String[] scopes, String state,
			OAuthResponseMode responseMode) {
		this(new OAuthResponseType[] { responseType }, clientId, redirectUri, scopes, state, responseMode);
	}

	public OAuthAuthorisationRequest(String clientId, String redirectUri, String[] scopes, String state, String codeChallenge,
			OAuthCodeChallengeMethod codeChallengeMethod, OAuthResponseMode responseMode) {
		this(OAuthResponseType.AUTH_CODE, clientId, redirectUri, scopes, state, responseMode);
		setCodeChallenge(codeChallenge);
		setCodeChallengeMethod(codeChallengeMethod);
	}

	public OAuthResponseType[] getResponseTypes() {
		String[] responseTypeStrings = getArrayFromSsv(getParam(OAuthParam.RESPONSE_TYPE));
		OAuthResponseType[] responseTypes = new OAuthResponseType[responseTypeStrings.length];
		for (int i = 0; i < responseTypes.length; i++) {
			responseTypes[i] = OAuthResponseType.fromString(responseTypeStrings[i]);
		}
		return responseTypes;
	}

	public void setResponseTypes(OAuthResponseType[] value) {
		if (value != null) {
			String[] responseTypeStrings = new String[value.length];
			for (int i = 0; i < value.length; i++) {
				responseTypeStrings[i] = value[i].toString();
			}
			setParam(OAuthParam.RESPONSE_TYPE, getSsvFromArray(responseTypeStrings));
		}
	}

	public void setResponseType(OAuthResponseType value) {
		setParam(OAuthParam.RESPONSE_TYPE, value.toString());
	}

	public String getClientId() {
		return getParam(OAuthParam.CLIENT_ID);
	}

	public void setClientId(String value) {
		setParam(OAuthParam.CLIENT_ID, value);
	}

	public String getRedirectUri() {
		return getParam(OAuthParam.REDIRECT_URI);
	}

	public void setRedirectUri(String value) {
		setParam(OAuthParam.REDIRECT_URI, value);
	}

	public String[] getScopes() {
		return getArrayFromSsv(getParam(OAuthParam.SCOPE));
	}

	public void setScopes(String[] value) {
		setParam(OAuthParam.SCOPE, getSsvFromArray(value));
	}

	public String getState() {
		return getParam(OAuthParam.STATE);
	}

	public void setState(String value) {
		setParam(OAuthParam.STATE, value);
	}

	public String getCodeChallenge() {
		return getParam(OAuthParam.CODE_CHALLENGE);
	}

	public void setCodeChallenge(String value) {
		setParam(OAuthParam.CODE_CHALLENGE, value);
	}

	public OAuthCodeChallengeMethod getCodeChallengeMethod() {
		return OAuthCodeChallengeMethod.fromString(getParam(OAuthParam.CODE_CHALLENGE_METHOD));
	}

	public void setCodeChallengeMethod(OAuthCodeChallengeMethod value) {
		setParam(OAuthParam.CODE_CHALLENGE_METHOD, value.toString());
	}

	public OAuthResponseMode getResponseMode() {
		return OAuthResponseMode.fromString(getParam(OAuthParam.RESPONSE_MODE));
	}

	public void setResponseMode(OAuthResponseMode value) {
		setParam(OAuthParam.RESPONSE_MODE, value.toString());
	}
}
