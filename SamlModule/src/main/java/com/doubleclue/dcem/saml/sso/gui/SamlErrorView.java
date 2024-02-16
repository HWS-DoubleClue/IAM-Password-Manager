package com.doubleclue.dcem.saml.sso.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.http.HttpStatus;

@SuppressWarnings("serial")
@SessionScoped
@Named("samlErrorView")
public class SamlErrorView extends SamlSsoView {

	private int httpStatusCode = HttpStatus.SC_BAD_GATEWAY;
	private int errorCode = -1;

	@Override
	public String getPageName() {
		return "samlErrorView";
	}

	public int getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
