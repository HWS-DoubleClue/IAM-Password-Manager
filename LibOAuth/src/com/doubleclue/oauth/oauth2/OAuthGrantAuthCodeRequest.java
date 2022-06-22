package com.doubleclue.oauth.oauth2;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.enums.OAuthGrantType;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;

@SuppressWarnings("serial")
public class OAuthGrantAuthCodeRequest extends OAuthGrantRequest {

	public OAuthGrantAuthCodeRequest(HttpServletRequest request) {
		super(request);
	}

	public OAuthGrantAuthCodeRequest(OAuthRequest request) {
		super(request);
	}

	public OAuthGrantAuthCodeRequest(String json) {
		super(json);
	}

	public OAuthGrantAuthCodeRequest(String redirectUri, String state, String authCode, String clientId, String codeVerifier) {
		super(OAuthGrantType.AUTH_CODE, state);
		setParam(OAuthParam.REDIRECT_URI, redirectUri);
		setParam(OAuthParam.AUTH_CODE, authCode);
		setParam(OAuthParam.CLIENT_ID, clientId);
		setParam(OAuthParam.CODE_VERIFIER, codeVerifier);
	}

	public String getRedirectUri() {
		return getParam(OAuthParam.REDIRECT_URI);
	}

	public String getAuthCode() {
		return getParam(OAuthParam.AUTH_CODE);
	}

	public String getCodeVerifier() {
		return getParam(OAuthParam.CODE_VERIFIER);
	}

	public String getClientAssertionType() {
		return getParam(OAuthParam.CLIENT_ASSERTION_TYPE);
	}

	public String getClientAssertion() {
		return getParam(OAuthParam.CLIENT_ASSERTION);
	}
}
