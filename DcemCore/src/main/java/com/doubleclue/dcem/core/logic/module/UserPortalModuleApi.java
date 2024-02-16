package com.doubleclue.dcem.core.logic.module;

import com.doubleclue.dcem.core.exceptions.DcemException;

public interface UserPortalModuleApi {

	public static final String USER_PORTAL_SERVICE_IMPL = "userPortalApiServiceImpl";

	public Object getUserPortalConfig() throws DcemException;
}