package com.doubleclue.dcup.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.doubleclue.dcup.logic.DcupConstants;

@SuppressWarnings("serial")
@Named("endMessageView")
@SessionScoped
public class EndMessageView extends AbstractPortalView {

	private String title;
	private boolean error;
	private String message;
	

	public String actionBackToLogin () {
		return DcupConstants.JSF_PAGE_LOGIN;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String getName() {
		return "ForgotPasswordMessageView";
	}

	@Override
	public String getPath() {
		return DcupConstants.JSF_PAGE_END_MESSAGE;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}