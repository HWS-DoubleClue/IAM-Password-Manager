package com.doubleclue.oauth.oauth2;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.enums.OAuthParam;

@SuppressWarnings("serial")
public class OAuthAuthCodeResponse extends OAuthRequest {

	public OAuthAuthCodeResponse(HttpServletRequest request) {
		super(request);
	}

	public OAuthAuthCodeResponse(OAuthRequest request) {
		paramMap.putAll(request.getParamMap());
	}

	public OAuthAuthCodeResponse(String json) {
		super(json);
	}

	public OAuthAuthCodeResponse(String authCode, String state) {
		setParam(OAuthParam.AUTH_CODE, authCode);
		setParam(OAuthParam.STATE, state);
	}

	public String getAuthCode() {
		return getParam(OAuthParam.AUTH_CODE);
	}

	public String getState() {
		return getParam(OAuthParam.STATE);
	}
}
