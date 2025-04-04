package com.doubleclue.dcem.admin.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.DcemView;

@SuppressWarnings("serial")
@Named("endMessageView")
@SessionScoped
public class EndMessageView extends DcemView {

	private String title;
	private boolean error;
	private String message;
	

	public String actionBackToLogin () {
		return "mgt/login.xhtml";
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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