package com.doubleclue.dcem.core.jersey;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.weld.CdiUtils;

/**
 * Jersey HTTP Basic Auth filter
 * 
 * @author Emanuel Galea
 */
@Provider
@PreMatching
public class RestServletResponseFilter implements ContainerResponseFilter {

	@Context
	private HttpServletRequest servletRequest;

	@Context
	private HttpServletResponse servletResponse;

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {

		if (servletRequest.getParameter("JSESSIONID") != null) {
			Cookie userCookie = new Cookie("JSESSIONID", servletRequest.getParameter("JSESSIONID"));
			((HttpServletResponse) servletRequest).addCookie(userCookie);
		} else {
			String sessionId = servletRequest.getSession().getId();
			Cookie userCookie = new Cookie("JSESSIONID", sessionId);
			servletResponse.addCookie(userCookie);
			// System.out.println("RestServletResponseFilter.filter() " +
			// sessionId);
		}
		Long startTime = (Long) requestContext.getProperty("startTime");
		if (startTime != null) {
			String path = requestContext.getUriInfo().getPath(true);
			int ind = path.indexOf('/');
			String moduleId = path.substring(0, ind);
			
			DcemApplicationBean dcemApplicationBean = CdiUtils.getReference(DcemApplicationBean.class);
			DcemModule module = dcemApplicationBean.getModule(moduleId);
			module.addCounter("REST-" + path.substring(ind), (System.currentTimeMillis() - startTime));

		}

	}

}