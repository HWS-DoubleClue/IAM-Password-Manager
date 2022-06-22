package com.doubleclue.dcem.as.restapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.doubleclue.dcem.as.restapi.impl.LoginApiServiceImpl;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.weld.CdiUtils;

@Path("/as/login")
@Consumes({ "application/json" })
@Produces({ "application/json" })

public class LoginApi {

	@GET
	@Path("/queryLoginQrCode")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })

	public Response queryLoginQrCode(@QueryParam("sessionId") String sessionId, @QueryParam("pollOnly") boolean pollOnly,
			@QueryParam("waitTimeSeconds") int waitTimeSeconds, @Context SecurityContext securityContext) throws DcemApiException {
		LoginApiServiceImpl apiServiceImpl = CdiUtils.getReference(LoginApiServiceImpl.class);

		return apiServiceImpl.queryLoginQrCode(sessionId, pollOnly, waitTimeSeconds, securityContext);
	}

	@GET
	@Path("/requestLoginQrCode")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })

	public Response requestLoginQrCode(@QueryParam("sessionId") String sessionId, @Context SecurityContext securityContext) throws DcemApiException {
		LoginApiServiceImpl apiServiceImpl = CdiUtils.getReference(LoginApiServiceImpl.class);

		return apiServiceImpl.requestLoginQrCode(sessionId, securityContext);
	}

	@GET
	@Path("/authenticate")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response authenticate(@QueryParam("userLoginId") String userLoginId, @QueryParam("authMethod") String authMethod,
			@QueryParam("password") String password, @QueryParam("passcode") String passcode, @QueryParam("networkAddress") String networkAddress,
			@QueryParam("fingerPrint") String fingerPrint, @QueryParam("ignorePassword") boolean ignorePassword,
			@QueryParam("fidoResponse") String fidoResponse, @QueryParam("rpId") String rpId, @Context SecurityContext securityContext)
			throws DcemApiException {
		LoginApiServiceImpl apiServiceImpl = CdiUtils.getReference(LoginApiServiceImpl.class);

		return apiServiceImpl.authenticate(userLoginId, authMethod, password, passcode, networkAddress, fingerPrint, ignorePassword, fidoResponse, rpId,
				securityContext);
	}

	@GET
	@Path("/authenticateMethods")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response getAuthenticateMethods(@Context SecurityContext securityContext) throws DcemApiException {

		LoginApiServiceImpl apiServiceImpl = CdiUtils.getReference(LoginApiServiceImpl.class);

		return apiServiceImpl.getAuthenticateMethods(securityContext);
	}
}
