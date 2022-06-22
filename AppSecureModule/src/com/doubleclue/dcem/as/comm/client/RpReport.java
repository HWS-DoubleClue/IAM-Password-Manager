package com.doubleclue.dcem.as.comm.client;

import java.util.Date;

public class RpReport {
	
	Date date;
	RpClientAction action;
	boolean success;
	String info;

	public RpReport(RpClientAction action, boolean success, String info) {
		super();
		this.date = new Date();
		this.action = action;
		this.success = success;
		this.info = info;
	}
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}

	public RpClientAction getAction() {
		return action;
	}

	public void setAction(RpClientAction action) {
		this.action = action;
	}

	public boolean isSussess() {
		return success;
	}

	public void setSussess(boolean sussess) {
		this.success = sussess;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

}
