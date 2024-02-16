package com.doubleclue.dcem.as.restapi.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.doubleclue.dcem.core.jersey.DcemApiException;


public class MiscApiServiceImpl  {
   
    public Response echo(String text, SecurityContext securityContext) throws DcemApiException {
        return Response.ok().entity(text).build();
    }
}
