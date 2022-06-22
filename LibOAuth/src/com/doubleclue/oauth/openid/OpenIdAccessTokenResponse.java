package com.doubleclue.oauth.openid;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.OAuthAccessTokenResponse;
import com.doubleclue.oauth.oauth2.OAuthRequest;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;
import com.doubleclue.oauth.oauth2.enums.OAuthTokenType;

@SuppressWarnings("serial")
public class OpenIdAccessTokenResponse extends OAuthAccessTokenResponse {

	public OpenIdAccessTokenResponse(HttpServletRequest request) {
		super(request);
	}

	public OpenIdAccessTokenResponse(OAuthRequest request) {
		super(request);
	}

	public OpenIdAccessTokenResponse(String json) {
		super(json);
	}

	public OpenIdAccessTokenResponse(String accessToken, Integer expiresIn, String refreshToken, String idToken) {
		super(accessToken, OAuthTokenType.BEARER, expiresIn, refreshToken);
		setParam(OAuthParam.ID_TOKEN, idToken);
	}

	public String getIdToken() {
		return getParam(OAuthParam.ID_TOKEN);
	}
}
