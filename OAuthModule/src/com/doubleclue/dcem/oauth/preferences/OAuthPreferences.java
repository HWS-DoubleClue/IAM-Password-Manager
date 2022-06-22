package com.doubleclue.dcem.oauth.preferences;

import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@SuppressWarnings("serial")
@XmlType
@XmlRootElement(name = "oauthPreferences")
public class OAuthPreferences extends ModulePreferences {

	@DcemGui(separator = "Authorisation Server Metadata", style = "width: 350px")
	private String issuer = null;

	@DcemGui(separator = "Token Configuration", style = "width: 50px")
	@Min(1)
	private int authCodeLifetime = 120;

	@DcemGui(style = "width: 50px")
	@Min(1)
	private int accessTokenLifetime = 3600;

	@DcemGui(style = "width: 50px")
	@Min(1)
	private int refreshTokenLifetime = 36000;

	@DcemGui(style = "width: 50px")
	@Min(1)
	private int idTokenLifetime = 10000;

	@DcemGui(separator = "SSO Service")
	private boolean passwordRequired = true;
	
	@DcemGui ()
	boolean enableDeviceWizard = true;

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public int getAuthCodeLifetime() {
		return authCodeLifetime;
	}

	public void setAuthCodeLifetime(int authCodeLifetime) {
		this.authCodeLifetime = authCodeLifetime;
	}

	public int getAccessTokenLifetime() {
		return accessTokenLifetime;
	}

	public void setAccessTokenLifetime(int accessTokenLifetime) {
		this.accessTokenLifetime = accessTokenLifetime;
	}

	public int getRefreshTokenLifetime() {
		return refreshTokenLifetime;
	}

	public void setRefreshTokenLifetime(int refreshTokenLifetime) {
		this.refreshTokenLifetime = refreshTokenLifetime;
	}

	public int getIdTokenLifetime() {
		return idTokenLifetime;
	}

	public void setIdTokenLifetime(int idTokenLifetime) {
		this.idTokenLifetime = idTokenLifetime;
	}

	public boolean isPasswordRequired() {
		return passwordRequired;
	}

	public void setPasswordRequired(boolean passwordRequired) {
		this.passwordRequired = passwordRequired;
	}

	public boolean isEnableDeviceWizard() {
		return enableDeviceWizard;
	}

	public void setEnableDeviceWizard(boolean enableDeviceWizard) {
		this.enableDeviceWizard = enableDeviceWizard;
	}
}
