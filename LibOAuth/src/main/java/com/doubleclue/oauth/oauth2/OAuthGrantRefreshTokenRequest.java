package com.doubleclue.oauth.oauth2;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.enums.OAuthGrantType;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;

@SuppressWarnings("serial")
public class OAuthGrantRefreshTokenRequest extends OAuthGrantRequest {

	public OAuthGrantRefreshTokenRequest(HttpServletRequest request) {
		super(request);
	}

	public OAuthGrantRefreshTokenRequest(OAuthRequest request) {
		super(request);
	}

	public OAuthGrantRefreshTokenRequest(String json) {
		super(json);
	}

	public OAuthGrantRefreshTokenRequest(String refreshToken, String state, String[] scopes) {
		super(OAuthGrantType.REFRESH_TOKEN, state, scopes);
		setParam(OAuthParam.REFRESH_TOKEN, refreshToken);
	}

	public String getRefreshToken() {
		return getParam(OAuthParam.REFRESH_TOKEN);
	}
}
