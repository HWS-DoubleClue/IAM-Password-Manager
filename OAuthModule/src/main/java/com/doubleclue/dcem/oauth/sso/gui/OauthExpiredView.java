package com.doubleclue.dcem.oauth.sso.gui;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@SuppressWarnings("serial")
@SessionScoped
@Named("oauthExpiredView")
public class OauthExpiredView extends OauthSsoView {
	
	@Override
	public String getPageName() {
		return "OauthExpiredView";
	}
}








