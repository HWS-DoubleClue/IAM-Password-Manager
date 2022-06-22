package com.doubleclue.dcem.saml.sso.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.saml.logic.SamlModule;
import com.doubleclue.dcem.saml.sso.logic.SamlSsoLogic;

@SuppressWarnings("serial")
@SessionScoped
@Named("samlReturnView")
public class SamlReturnView extends SamlSsoView {

	@Inject
	SamlModule samlModule;
	
	@Inject
	SamlSsoLogic ssoServiceLogic;

	private String base64Response;
	private String acsLocation;
	private String relayToken;
	private String entityId;
	private String displayName;

	@Override
	public String getPageName() {
		return "samlReturnView";
	}

	public boolean getAutoPost() {
		boolean result = samlModule.getModulePreferences().isAutoRedirectToSp() && base64Response != null && acsLocation != null;
		if (result == false) {
			JsfUtils.addErrorMessage(ssoServiceLogic.getDbResourceBundle(), "sso.error.noRedirection");
		}
		return result;
	}

	public String getBase64Response() {
		return base64Response;
	}

	public void setBase64Response(String base64Response) {
		this.base64Response = base64Response;
	}

	public String getAcsLocation() {
		return acsLocation;
	}

	public void setAcsLocation(String acsLocation) {
		this.acsLocation = acsLocation;
	}

	public String getRelayToken() {
		return relayToken;
	}

	public void setRelayToken(String relayToken) {
		this.relayToken = relayToken;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
}
