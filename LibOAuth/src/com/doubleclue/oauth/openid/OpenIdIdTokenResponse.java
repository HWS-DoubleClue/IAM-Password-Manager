package com.doubleclue.oauth.openid;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.OAuthRequest;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;

@SuppressWarnings("serial")
public class OpenIdIdTokenResponse extends OAuthRequest {

	public OpenIdIdTokenResponse(HttpServletRequest request) {
		super(request);
	}

	public OpenIdIdTokenResponse(OAuthRequest request) {
		super(request.getParamMap());
	}

	public OpenIdIdTokenResponse(String json) {
		super(json);
	}

	public OpenIdIdTokenResponse(String idToken, String state) {
		setParam(OAuthParam.ID_TOKEN, idToken);
		setParam(OAuthParam.STATE, state);
	}

	public String getIdToken() {
		return getParam(OAuthParam.ID_TOKEN);
	}

	public String getState() {
		return getParam(OAuthParam.STATE);
	}
}
