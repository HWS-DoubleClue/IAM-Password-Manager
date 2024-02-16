package com.doubleclue.dcem.as.logic;

public class PushNotificationConfig {
	
	boolean enable = true;
	boolean inherit = false;
	boolean passwordLessPushApproval;
	String googleServiceFile;
	
	public PushNotificationConfig() {
		
	}

	public PushNotificationConfig(boolean enable, boolean passwordLessPushApproval, String googleServiceFile) {
		super();
		this.enable = enable;
		this.passwordLessPushApproval = passwordLessPushApproval;
		this.googleServiceFile = googleServiceFile;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public boolean isPasswordLessPushApproval() {
		return passwordLessPushApproval;
	}

	public void setPasswordLessPushApproval(boolean passwordLessPushApproval) {
		this.passwordLessPushApproval = passwordLessPushApproval;
	}

	public String getGoogleServiceFile() {
		return googleServiceFile;
	}

	public void setGoogleServiceFile(String googleServiceFile) {
		this.googleServiceFile = googleServiceFile;
	}

	public boolean isInherit() {
		return inherit;
	}

	public void setInherit(boolean inherit) {
		this.inherit = inherit;
	}
	
	
	
	
}
