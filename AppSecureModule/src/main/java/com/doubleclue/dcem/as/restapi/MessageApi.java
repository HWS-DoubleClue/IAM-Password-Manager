package com.doubleclue.dcem.as.restapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.doubleclue.dcem.as.restapi.impl.MessageApiServiceImpl;
import com.doubleclue.dcem.as.restapi.model.AsApiMessage;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.weld.CdiUtils;

@Path("/as/message")
@Consumes({ "application/json" })
@Produces({ "application/json" })

public class MessageApi {

	@POST
	@Path("/add")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })

	public Response addMessage(AsApiMessage apiMessage, @Context SecurityContext securityContext) throws DcemApiException {
		MessageApiServiceImpl apiServiceImpl = CdiUtils.getReference(MessageApiServiceImpl.class);
		securityContext.getAuthenticationScheme();
		return apiServiceImpl.addMessage(apiMessage);
	}

	@POST
	@Path("/cancel")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })

	public Response cancelMessage(@QueryParam("msgId") Long msgId, @Context SecurityContext securityContext)
			throws DcemApiException {
		MessageApiServiceImpl apiServiceImpl = CdiUtils.getReference(MessageApiServiceImpl.class);
		return apiServiceImpl.cancelMessage(msgId, securityContext);
	}

	@POST
	@Path("/cancelUserMessages")
	@Consumes({ "application/json" })
	@Produces({ "application/json" })

	public Response cancelUserMessages(@QueryParam("msgId") String msgId, @Context SecurityContext securityContext)
			throws DcemApiException {
		MessageApiServiceImpl apiServiceImpl = CdiUtils.getReference(MessageApiServiceImpl.class);
		return apiServiceImpl.cancelUserMessages(msgId, securityContext);
	}

	@GET

	@Consumes({ "application/json" })
	@Produces({ "application/json" })

	public Response getMessageResponse(@QueryParam("msgId") Long msgId,
			@DefaultValue("0") @QueryParam("waitTimeSeconds") int waitTimeSeconds,
			@Context SecurityContext securityContext) throws DcemApiException {
		MessageApiServiceImpl apiServiceImpl = CdiUtils.getReference(MessageApiServiceImpl.class);
		return apiServiceImpl.getMessageResponse(msgId, waitTimeSeconds, securityContext);
	}
}
