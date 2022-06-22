package com.doubleclue.oauth.oauth2;

import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.enums.OAuthGrantType;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;

@SuppressWarnings("serial")
public class OAuthGrantRequest extends OAuthRequest {

	public OAuthGrantRequest(HttpServletRequest request) {
		super(request);
		String authorisation = request.getHeader("Authorization");
		if (authorisation != null && authorisation.startsWith("Basic ")) {
			byte[] decodedBytes = Base64.getDecoder().decode(authorisation.substring(6));
			String decodedString = new String(decodedBytes);
			if (decodedString.contains(":")) {
				String[] creds = decodedString.split(":");
				if (creds.length >= 2) {
					setParam(OAuthParam.CLIENT_ID, creds[0]);
					setParam(OAuthParam.CLIENT_SECRET, creds[1]);
				}
			}
		}
	}

	public OAuthGrantRequest(OAuthRequest request) {
		paramMap.putAll(request.getParamMap());
	}

	public OAuthGrantRequest(String json) {
		super(json);
	}

	public OAuthGrantRequest(OAuthGrantType grantType, String state) {
		setParam(OAuthParam.GRANT_TYPE, grantType.toString());
		setParam(OAuthParam.STATE, state);
	}

	public OAuthGrantRequest(OAuthGrantType grantType, String state, String[] scopes) {
		this(grantType, state);
		setParam(OAuthParam.SCOPE, getSsvFromArray(scopes));
	}

	public OAuthGrantRequest(OAuthGrantType grantType, String state, String[] scopes, String clientId, String clientSecret) {
		this(grantType, state, scopes);
		setParam(OAuthParam.CLIENT_ID, clientId);
		setParam(OAuthParam.CLIENT_SECRET, clientSecret);
	}

	public OAuthGrantType getGrantType() {
		return OAuthGrantType.fromString(getParam(OAuthParam.GRANT_TYPE));
	}

	public String[] getScopes() {
		return getArrayFromSsv(getParam(OAuthParam.SCOPE));
	}

	public String getClientId() {
		return getParam(OAuthParam.CLIENT_ID);
	}

	public String getClientSecret() {
		return getParam(OAuthParam.CLIENT_SECRET);
	}

	public String getState() {
		return getParam(OAuthParam.STATE);
	}
}
