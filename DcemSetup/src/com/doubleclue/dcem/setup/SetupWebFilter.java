package com.doubleclue.dcem.setup;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author galea
 * 
 */
@WebFilter(filterName = "LoginWebFilter", urlPatterns = "/mgt/*")
public class SetupWebFilter implements Filter {



	private static final String FACES_REQUEST = "Faces-Request";
	private static final String FACES_AJAX_REQUEST = "partial/ajax";
	private static final String WEB_NAME = "setup";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
//		String path = httpServletRequest.getServletPath();
//		System.out.println("SetupWebFilter.doFilter() Path: " + path);
		if (isSessionControlRequiredForThisResource(httpServletRequest)) {
			if (isSessionInvalid(httpServletRequest) && httpServletRequest.getParameter("start") == null ) {
				if (httpServletRequest.getParameter("start") == null) 
				// For Ajax Request
				if (isAjaxRequest(httpServletRequest)) {
					response.setContentType("text/xml");
					response.getWriter().append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").printf(
							"<partial-response><redirect url=\"/%s/%s\"></redirect></partial-response>", WEB_NAME, "setupExpired.xhtml");
				} else {
					httpServletResponse.sendRedirect("/" + WEB_NAME + "/setupExpired.xhtml");
				}
				return;
			}

		}
		chain.doFilter(request, response);
	}

	

	@Override
	public void destroy() {
		// nothing to do
	}

	private boolean isAjaxRequest(HttpServletRequest httpServletRequest) {
		return FACES_AJAX_REQUEST.equals(httpServletRequest.getHeader(FACES_REQUEST));
	}


	/*
	 * session shouldn't be checked for some pages. For example: for timeout
	 * page.. Since we're redirecting to timeout page from this filter, if we
	 * don't disable session control for it, filter will again redirect to it
	 * and this will be result with an infinite loop...
	 */
	private boolean isSessionControlRequiredForThisResource(HttpServletRequest httpServletRequest) {

		String requestPath = httpServletRequest.getServletPath();

		// System.out.println("LoginFilter.isSessionControlRequiredForThisResource()
		// " + requestPath);

		if (requestPath == null)
			return false;

		String sessionControlStr = (String) httpServletRequest.getAttribute("isSessionControlRequired");

		boolean isSessionControlRequired = (sessionControlStr == null || "true".equals(sessionControlStr)) ? true : false;

		return (!requestPath.endsWith("Expired.xhtml") && !requestPath.startsWith("/org.") && !requestPath.startsWith("/javax")
				&& !requestPath.equals(httpServletRequest.getContextPath() + "/") && isSessionControlRequired);

	}

	private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
		boolean sessionInValid = (httpServletRequest.getRequestedSessionId() != null) && !httpServletRequest.isRequestedSessionIdValid();
		return sessionInValid;
	}
}
