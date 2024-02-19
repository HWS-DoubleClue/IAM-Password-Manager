package com.doubleclue.oauth.oauth2;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.enums.OAuthGrantType;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;

@SuppressWarnings("serial")
public class OAuthGrantRopcRequest extends OAuthGrantRequest {

	public OAuthGrantRopcRequest(HttpServletRequest request) {
		super(request);
	}

	public OAuthGrantRopcRequest(OAuthRequest request) {
		super(request);
	}

	public OAuthGrantRopcRequest(String json) {
		super(json);
	}

	public OAuthGrantRopcRequest(String state, String username, String password, String[] scopes) {
		super(OAuthGrantType.PASSWORD, state, scopes);
		setParam(OAuthParam.USERNAME, username);
		setParam(OAuthParam.PASSWORD, password);
	}

	public OAuthGrantRopcRequest(String state, String username, String password, String[] scopes, String clientId, String clientSecret) {
		super(OAuthGrantType.PASSWORD, state, scopes, clientId, clientSecret);
		setParam(OAuthParam.USERNAME, username);
		setParam(OAuthParam.PASSWORD, password);
	}

	public String getUsername() {
		return getParam(OAuthParam.USERNAME);
	}

	public String getPassword() {
		return getParam(OAuthParam.PASSWORD);
	}
}
