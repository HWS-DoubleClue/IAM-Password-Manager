package com.doubleclue.dcem.saml.sso.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.saml.sso.logic.SamlSsoLogic;

@SuppressWarnings("serial")
@SessionScoped
@Named("samlLogoutView")
public class SamlLogoutView extends SamlSsoView {

	@Inject
	SamlSsoLogic ssoLogic;

	@Override
	public String getPageName() {
		return "samlLogoutView";
	}

	public boolean getRespondsToRequest() {
		return ssoLogic.getLogoutRequest() != null && ssoLogic.getMetadata().getLogoutLocation() != null;
	}

	public String getBase64Response() {

		if (getRespondsToRequest()) {
			try {
				return ssoLogic.createLogoutResponse();
			} catch (Exception e) {
				logger.info("SAML - Failed to create logout response: " + e.toString());
			}
		}

		return "";
	}

	public String getLogoutLocation() {
		return getRespondsToRequest() ? ssoLogic.getMetadata().getLogoutLocation() : "";
	}

	public String getLogoutMethod() {
		if (getRespondsToRequest()) {
			return ssoLogic.getMetadata().isLogoutIsPost() ? "post" : "get";
		}
		return "";
	}

	public String getRelayToken() {
		return getRespondsToRequest() ? ssoLogic.getRelayToken() : "";
	}

	public String getDisplayName() {
		return getRespondsToRequest() ? ssoLogic.getMetadata().getDisplayName() : "";
	}
}
