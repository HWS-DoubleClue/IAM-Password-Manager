package com.doubleclue.dcem.as.restapi;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.logic.module.AsApiOtpToken;
import com.doubleclue.dcem.core.logic.module.OtpModuleApi;
import com.doubleclue.dcem.core.weld.CdiUtils;

@Path("as/token")
@Consumes({ "application/json" })
@Produces({ "application/json" })

public class TokenApi {

	@POST
	@Path("/modifyOtpToken")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response modifyOtpToken(AsApiOtpToken asApiOtpToken, @QueryParam("passcode") String passcode, @Context SecurityContext securityContext)
			throws DcemApiException {
		OtpModuleApi apiServiceImpl = CdiUtils.getReference(OtpModuleApi.OTP_SERVICE_IMPL);
		if (apiServiceImpl == null) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(0, "otpApiServiceImpl", null)).build();
		}
		try {
			apiServiceImpl.modifyOtpToken(asApiOtpToken, passcode);
		} catch (DcemException exp) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
		return Response.ok().build();

	}

	@POST
	@Path("/queryOtpTokens")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })

	public Response queryOtpTokens(List<ApiFilterItem> filterItems, @QueryParam("offset") int offset, @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {

		OtpModuleApi apiServiceImpl = CdiUtils.getReference(OtpModuleApi.OTP_SERVICE_IMPL);
		if (apiServiceImpl == null) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(0, "otpApiServiceImpl", null)).build();
		}
		return apiServiceImpl.queryOtpTokens(filterItems, offset, maxResults, securityContext);
	}

}
