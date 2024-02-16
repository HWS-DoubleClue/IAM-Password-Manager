package com.doubleclue.dcem.as.restapi.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.doubleclue.dcem.core.jersey.DcemApiException;


public class EchoApiServiceImpl  {
 

    public Response echo(String text, SecurityContext securityContext) throws DcemApiException {
        // do some magic!
        return Response.ok().entity(text).build();
    }
}
