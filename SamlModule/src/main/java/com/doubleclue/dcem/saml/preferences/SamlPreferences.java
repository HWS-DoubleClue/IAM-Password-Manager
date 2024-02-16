package com.doubleclue.dcem.saml.preferences;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@SuppressWarnings("serial")
@XmlType
@XmlRootElement(name = "samlPreferences")
public class SamlPreferences extends ModulePreferences {

	@DcemGui(separator = "IdP Metadata", style = "width: 350px")
	private String ssoDomain = null;

	@DcemGui(style = "width: 350px")
	private String idpEntityId = null;

	@DcemGui(separator = "SSO Service")
	private boolean passwordRequired = true;

	@DcemGui()
	private boolean autoRedirectToSp = false;

	@DcemGui(style = "width: 50px")
	@Min(1)
	@Max(30)
	int sessionIdleTimeout = 5;
	
	@DcemGui ()
	boolean enableDeviceWizard = true;
	

	public String getSsoDomain() {
		return ssoDomain;
	}

	public void setSsoDomain(String ssoDomain) {
		this.ssoDomain = ssoDomain;
	}

	public String getIdpEntityId() {
		return idpEntityId;
	}

	public void setIdpEntityId(String entityId) {
		this.idpEntityId = entityId;
	}

	public boolean isPasswordRequired() {
		return passwordRequired;
	}

	public void setPasswordRequired(boolean passwordRequired) {
		this.passwordRequired = passwordRequired;
	}
	
	public boolean isAutoRedirectToSp() {
		return autoRedirectToSp;
	}

	public void setAutoRedirectToSp(boolean autoRedirectToSp) {
		this.autoRedirectToSp = autoRedirectToSp;
	}

	public int getSessionIdleTimeout() {
		return sessionIdleTimeout;
	}

	public void setSessionIdleTimeout(int sessionIdleTimeout) {
		this.sessionIdleTimeout = sessionIdleTimeout;
	}

	public boolean isEnableDeviceWizard() {
		return enableDeviceWizard;
	}

	public void setEnableDeviceWizard(boolean enableDeviceWizard) {
		this.enableDeviceWizard = enableDeviceWizard;
	}
}
