package com.doubleclue.portaldemo.gui;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import com.doubleclue.portaldemo.AbstractPortalView;

@SuppressWarnings("serial")
@Named("welcomeTabView")
@RequestScoped
public class WelcomeTabView extends AbstractPortalView {

	public void actionSend() {

	}

	@Override
	public String getName() {
		return "WelcomeTabView";
	}

	@Override
	public String getPath() {
		return "welcomeTabView.xhtml";
	}

}
