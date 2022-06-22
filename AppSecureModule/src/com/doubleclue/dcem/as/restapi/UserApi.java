package com.doubleclue.dcem.as.restapi;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.doubleclue.dcem.as.restapi.impl.UserApiServiceImpl;
import com.doubleclue.dcem.as.restapi.model.AsApiActivationCode;
import com.doubleclue.dcem.as.restapi.model.AsApiUrlToken;
import com.doubleclue.dcem.as.restapi.model.AsApiUser;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.weld.CdiUtils;

@Path("as/user")
@Consumes({ "application/json" })
@Produces({ "application/json" })
public class UserApi {

	@POST
	@Path("/addActivationCode")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response addActivationCode(AsApiActivationCode activationCode, @Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.addActivationCode(activationCode, securityContext);
	}

	@POST
	@Path("/addUser")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response addUser(AsApiUser user, @Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.addUser(user, securityContext);
	}

	@GET
	@Path("/getUser")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response getUser(@QueryParam("loginId") String loginId, @Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.getUser(loginId, securityContext);
	}

	@POST
	@Path("/modifyUser")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response modifyUser(AsApiUser user, @Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.modifyUser(user, securityContext);
	}

	@POST
	@Path("/queryUsers")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response queryUsers(List<ApiFilterItem> filterItems, @QueryParam("offset") int offset, @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.queryUsers(filterItems, offset, maxResults, securityContext);
	}

	@GET
	@Path("/deleteUser")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response deleteUser(@QueryParam("loginId") String loginId, @Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.deleteUser(loginId, securityContext);
	}

	@POST
	@Path("/queryActivationCodes")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response queryActivationCodes(List<ApiFilterItem> filterItems, @QueryParam("offset") int offset, @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.queryActivationCodes(filterItems, offset, maxResults, securityContext);
	}

	@POST
	@Path("/deleteActivationCode")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response deleteActivationCode(@QueryParam("activationCodeId") int activationCodeId, @Context SecurityContext securityContext)
			throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.deleteActivationCode(activationCodeId, securityContext);
	}

	@POST
	@Path("/changePassword")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response changePassword(@QueryParam("userLoginId") String userLoginId, @QueryParam("oldPassword") String oldPassword,
			@QueryParam("newPassword") String newPassword, @Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.changePassword(userLoginId, oldPassword, newPassword, securityContext);
	}

	@POST
	@Path("/setPassword")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response setPassword(@QueryParam("userLoginId") String userLoginId, @QueryParam("newPassword") String newPassword,
			@Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.setPassword(userLoginId, newPassword, securityContext);
	}

	@POST
	@Path("/addUrlToken")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response addUrlToken(AsApiUrlToken urlToken, @Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.addUrlToken(urlToken, securityContext);
	}

	@POST
	@Path("/verifyUrlToken")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response verifyUrlToken(AsApiUrlToken urlToken, @Context SecurityContext securityContext) throws DcemApiException {
		UserApiServiceImpl apiServiceImpl = CdiUtils.getReference(UserApiServiceImpl.class);
		return apiServiceImpl.verifyUrlToken(urlToken, securityContext);
	}
}