package com.doubleclue.dcem.as.restapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.doubleclue.dcem.as.restapi.impl.MiscApiServiceImpl;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.weld.CdiUtils;

@Path("/as")
@Consumes({ "application/json", "text/plain" })
@Produces({ "application/json" })

public class MiscApi {

	@GET
	@Path("/misc/echo")
	@Consumes({ "text/plain"  })
	@Produces({ "application/json", "text/xml", "text/html" })

	public Response echo(@QueryParam("text") String text, @Context SecurityContext securityContext)
			throws DcemApiException {

		MiscApiServiceImpl apiServiceImpl = CdiUtils.getReference(MiscApiServiceImpl.class);
		return apiServiceImpl.echo(text, securityContext);
	}
}
