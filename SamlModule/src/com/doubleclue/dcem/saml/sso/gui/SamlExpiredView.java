package com.doubleclue.dcem.saml.sso.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@SuppressWarnings("serial")
@SessionScoped
@Named("samlExpiredView")
public class SamlExpiredView extends SamlSsoView {

	@Override
	public String getPageName() {
		return "samlExpiredView";
	}

}
