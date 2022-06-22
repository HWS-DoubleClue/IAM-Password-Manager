package com.doubleclue.dcem.saml.sso.gui;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SamlSsoView implements Serializable {

	private static final long serialVersionUID = 1L;
	protected static final Logger logger = LogManager.getLogger(SamlSsoView.class);

	public abstract String getPageName();
}
