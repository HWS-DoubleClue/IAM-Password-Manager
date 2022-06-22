package com.doubleclue.portaldemo;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.doubleclue.portaldemo.utils.PortalUtils;

/**
 * @author galea
 * 
 */
// WebFilter(filterName = "LoginPortalFilter", urlPatterns = "/*")
public class LoginPortalFilter implements Filter {

	// private static final String URL_FACES = "/faces";
	private static final String URL_STYLE = "/style";
	private static final String URL_PICTURES = "/pictures";
	private static final String URL_OPEN = "/open";

	private static final String FACES_REQUEST = "Faces-Request";
	private static final String FACES_AJAX_REQUEST = "partial/ajax";
	private static final String LOGIN_URL = "portalLogin.xhtml";
	// private static final String TIMEOUT_PAGE = "faces/index.jsp";

	private static final String URL_ORG = "/org.";
	private static final String URL_JAVAX = "/javax.";

	private String webName = "portalDemo";

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		HttpServletResponse httpServletResponse = (HttpServletResponse) response;
		// if (localeBean.getStartupLocale() == null) {
		// localeBean.setLocale(httpServletRequest.getLocale());
		// }
		// check Session Expiration

		if (isSessionControlRequiredForThisResource(httpServletRequest)) {
			if (isSessionInvalid(httpServletRequest)) {
				// For Ajax Request
				if (isAjaxRequest(httpServletRequest)) {
					response.setContentType("text/xml");
					response.getWriter().append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").printf(
							"<partial-response><redirect url=\"/%s/%s\"></redirect></partial-response>", webName,
							"open/expired.xhtml");
				} else {
					httpServletResponse.sendRedirect("/" + webName + "/open/expired.xhtml");
				}
				return;
			}

		}
		
//		System.out.println("LoginPortalFilter.doFilter() Session=" + httpServletRequest.getSession().getId());

		String path = httpServletRequest.getServletPath();
//		System.out.println("LoginPortalFilter.doFilter() " + path);
		if (excludeFromFilter(path) == true) {
			chain.doFilter(request, response);
			return;
		}

		PortalSessionBean portalSessionBean = PortalUtils.getReference(PortalSessionBean.class);
		if (portalSessionBean != null && portalSessionBean.isLoggedIn()) {
			chain.doFilter(request, response);
			return;
		}

		// redirect to html page
		StringBuilder sb = new StringBuilder();
		sb.append(httpServletRequest.getContextPath());
		sb.append("/");
		sb.append(LOGIN_URL);

		// For Ajax Request
		if (isAjaxRequest(httpServletRequest)) {
			response.setContentType("text/xml");
			response.getWriter().append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
					.printf("<partial-response><redirect url=\"%s\"></redirect></partial-response>", sb.toString());
		} else {
			((HttpServletRequest) request).getSession().setMaxInactiveInterval(2);
			httpServletResponse.sendRedirect(sb.toString());
			// dispose session after 2 seconds

		}
		return;
	}

	@Override
	public void destroy() {
		// nothing to do
	}

	private boolean isAjaxRequest(HttpServletRequest httpServletRequest) {
		return FACES_AJAX_REQUEST.equals(httpServletRequest.getHeader(FACES_REQUEST));
	}

	private boolean excludeFromFilter(String path) {

		assert (path != null && !path.trim().isEmpty()) : "path must not be null or empty";
		// System.out.println("LoginWebFilter.excludeFromFilter() " + path);

		// exclude these paths
		if (path.startsWith(URL_OPEN) || path.startsWith(URL_PICTURES) || path.startsWith(URL_STYLE)
				|| path.startsWith(URL_ORG) || path.startsWith(URL_JAVAX) || path.endsWith("Login.xhtml") || 
				path.endsWith("Config.xhtml") || path.endsWith(".js")) {
			return true;
		}return false;

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

		boolean isSessionControlRequired = (sessionControlStr == null || "true".equals(sessionControlStr)) ? true
				: false;

		return (!requestPath.startsWith("/open/") && !requestPath.startsWith("/org.")
				&& !requestPath.startsWith("/javax") && !requestPath.equals(httpServletRequest.getContextPath() + "/")
				&& isSessionControlRequired);

	}

	private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
		boolean sessionInValid = (httpServletRequest.getRequestedSessionId() != null)
				&& !httpServletRequest.isRequestedSessionIdValid();
		return sessionInValid;
	}
}
