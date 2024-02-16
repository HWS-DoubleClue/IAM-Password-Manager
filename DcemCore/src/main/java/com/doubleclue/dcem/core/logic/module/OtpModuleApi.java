package com.doubleclue.dcem.core.logic.module;

import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;

public interface OtpModuleApi {

	public static final String OTP_SERVICE_IMPL = "otpApiServiceImpl";

	public String verifyOtpPasscode(DcemUser user, String passcode) throws DcemException;

	void modifyOtpToken(AsApiOtpToken asApiOtpToken, String passcode) throws DcemException;

	Response queryOtpTokens(List<ApiFilterItem> filters, Integer offset, Integer maxResults,
			SecurityContext securityContext);

	List<AsApiOtpToken> queryOtpTokenEntities(List<ApiFilterItem> filters, Integer offset, Integer maxResults,
			boolean includeSecretKey) throws DcemException;

	int getDelayWindow();
}