package com.doubleclue.oauth.oauth2;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.enums.OAuthError;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;

@SuppressWarnings("serial")
public class OAuthErrorResponse extends OAuthRequest {

	public OAuthErrorResponse(HttpServletRequest request) {
		super(request);
	}

	public OAuthErrorResponse(OAuthRequest request) {
		paramMap.putAll(request.getParamMap());
	}

	public OAuthErrorResponse(String json) {
		super(json);
	}

	public OAuthErrorResponse(OAuthError error, String errorDescription, String errorUri, String state) {
		setParam(OAuthParam.ERROR, error.toString());
		setParam(OAuthParam.ERROR_DESCRIPTION, errorDescription);
		setParam(OAuthParam.ERROR_URI, errorUri);
		setParam(OAuthParam.STATE, state);
	}

	public OAuthError getError() {
		return OAuthError.fromString(getParam(OAuthParam.ERROR));
	}

	public String getErrorDescription() {
		return getParam(OAuthParam.ERROR_DESCRIPTION);
	}

	public String getErrorUri() {
		return getParam(OAuthParam.ERROR_URI);
	}

	public String getState() {
		return getParam(OAuthParam.STATE);
	}
}
