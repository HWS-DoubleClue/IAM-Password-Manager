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

import com.doubleclue.dcem.as.restapi.impl.DeviceApiServiceImpl;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.weld.CdiUtils;

@Path("as/device")
@Consumes({ "application/json" })
@Produces({ "application/json" })

public class DeviceApi {

	@POST
	@Path("/queryDevices")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })

	public Response queryDevices(List<ApiFilterItem> filterItems, @QueryParam("offset") int offset, @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {

		DeviceApiServiceImpl apiServiceImpl = CdiUtils.getReference(DeviceApiServiceImpl.class);
		return apiServiceImpl.queryDevices(filterItems, offset, maxResults, securityContext);
	}

	@POST
	@Path("/deleteDevice")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response deleteDevice(@QueryParam("deviceId") int deviceId, @Context SecurityContext securityContext) throws DcemApiException {
		DeviceApiServiceImpl apiServiceImpl = CdiUtils.getReference(DeviceApiServiceImpl.class);
		return apiServiceImpl.deleteDevice(deviceId, securityContext);
	}

	@POST
	@Path("/setDeviceState")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })

	public Response setDeviceState(@QueryParam("deviceId") int deviceId, @QueryParam("enableState") boolean enableState,
			@Context SecurityContext securityContext) throws DcemApiException {
		DeviceApiServiceImpl apiServiceImpl = CdiUtils.getReference(DeviceApiServiceImpl.class);
		return apiServiceImpl.setDeviceState(deviceId, enableState, securityContext);
	}

	@POST
	@Path("/fidoStartRegistration")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response fidoStartRegistration(@QueryParam("username") String username, @QueryParam("rpId") String rpId,
			@Context SecurityContext securityContext) throws DcemApiException {
		DeviceApiServiceImpl apiServiceImpl = CdiUtils.getReference(DeviceApiServiceImpl.class);
		return apiServiceImpl.fidoStartRegistration(username, rpId, securityContext);
	}

	@POST
	@Path("/fidoFinishRegistration")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response fidoFinishRegistration(@QueryParam("responseJson") String responseJson, @QueryParam("displayName") String displayName,
			@Context SecurityContext securityContext) throws DcemApiException {
		DeviceApiServiceImpl apiServiceImpl = CdiUtils.getReference(DeviceApiServiceImpl.class);
		return apiServiceImpl.fidoFinishRegistration(responseJson, displayName, securityContext);
	}

	@POST
	@Path("/queryFidoAuthenticators")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response queryFidoAuthenticators(List<ApiFilterItem> filterItems, @QueryParam("offset") int offset, @QueryParam("maxResults") int maxResults,
			@Context SecurityContext securityContext) throws DcemApiException {
		DeviceApiServiceImpl apiServiceImpl = CdiUtils.getReference(DeviceApiServiceImpl.class);
		return apiServiceImpl.queryFidoAuthenticators(filterItems, offset, maxResults, securityContext);
	}

	@POST
	@Path("/deleteFidoAuthenticator")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })
	public Response deleteFidoAuthenticator(@QueryParam("fidoAuthenticatorId") int fidoAuthenticatorId, @Context SecurityContext securityContext)
			throws DcemApiException {
		DeviceApiServiceImpl apiServiceImpl = CdiUtils.getReference(DeviceApiServiceImpl.class);
		return apiServiceImpl.deleteFidoAuthenticator(fidoAuthenticatorId, securityContext);
	}
}
