package com.doubleclue.oauth.oauth2;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.enums.OAuthParam;
import com.doubleclue.oauth.oauth2.enums.OAuthTokenType;

@SuppressWarnings("serial")
public class OAuthAccessTokenResponse extends OAuthRequest {

	public OAuthAccessTokenResponse(HttpServletRequest request) {
		super(request);
	}

	public OAuthAccessTokenResponse(OAuthRequest request) {
		paramMap.putAll(request.getParamMap());
	}

	public OAuthAccessTokenResponse(String json) {
		super(json);
	}

	public OAuthAccessTokenResponse(String accessToken, OAuthTokenType tokenType, Integer expiresIn) {
		setParam(OAuthParam.ACCESS_TOKEN, accessToken);
		setParam(OAuthParam.TOKEN_TYPE, tokenType.toString());
		setParam(OAuthParam.EXPIRES_IN, expiresIn);
	}

	public OAuthAccessTokenResponse(String accessToken, OAuthTokenType tokenType, Integer expiresIn, String refreshToken) {
		this(accessToken, tokenType, expiresIn);
		setParam(OAuthParam.REFRESH_TOKEN, refreshToken);
	}

	public OAuthAccessTokenResponse(String accessToken, OAuthTokenType tokenType, Integer expiresIn, String[] scopes, String state) {
		this(accessToken, tokenType, expiresIn);
		setParam(OAuthParam.SCOPE, getSsvFromArray(scopes));
		setParam(OAuthParam.STATE, state);
	}

	public String getAccessToken() {
		return getParam(OAuthParam.ACCESS_TOKEN);
	}

	public OAuthTokenType getTokenType() {
		return OAuthTokenType.fromString(getParam(OAuthParam.TOKEN_TYPE));
	}

	public Integer getExpiresIn() {
		return getParam(OAuthParam.EXPIRES_IN);
	}

	public String getRefreshToken() {
		return getParam(OAuthParam.REFRESH_TOKEN);
	}

	public String[] getScopes() {
		return getArrayFromSsv(getParam(OAuthParam.SCOPE));
	}

	public String getState() {
		return getParam(OAuthParam.STATE);
	}
}
