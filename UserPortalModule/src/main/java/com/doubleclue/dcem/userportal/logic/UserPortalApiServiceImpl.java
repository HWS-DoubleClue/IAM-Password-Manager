package com.doubleclue.dcem.userportal.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.logic.module.UserPortalModuleApi;

@Named("userPortalApiServiceImpl")
@ApplicationScoped
public class UserPortalApiServiceImpl implements UserPortalModuleApi {

	@Inject
	UserPortalModule userPortalModule;

	@Override
	public Object getUserPortalConfig() throws DcemException {
		return userPortalModule.getModulePreferences();
	}
}
