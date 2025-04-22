package com.doubleclue.dcem.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;

// WebServlet("/DcemExceptionHandler")
@SuppressWarnings("serial")
public class DcemExceptionHandler extends HttpServlet {


	@Inject
	AdminModule module;

	private final static Logger logger = LogManager.getLogger(DcemExceptionHandler.class);

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processError(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		processError(request, response);
	}

	private void processError(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// Analyze the servlet exception
		// request.getSession().invalidate();
		Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
		String servletName = (String) request.getAttribute("javax.servlet.error.servlet_name");
		String msg = (String) request.getAttribute("javax.servlet.error.message");
		String uri = (String) request.getAttribute("javax.servlet.error.request_uri");

		if (servletName == null) {
			servletName = "Unknown";
		}

	 	request.getParameterMap();
		String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
		if (requestUri == null) {
			requestUri = "Unknown";
		}
		logger.warn("DcemExceptionHandler: Status=" + statusCode + " URI: " + uri + ", Servlet=" + servletName + ", Msg=" + msg, throwable);
		if (servletName.equals("jersey")) {
			return;
		}
		String error = "Unexpected ERROR";
	//	String attribute = (String) request.getAttribute("error-message");

		if (statusCode != null) {

			switch (statusCode) {
			case 500:
				error = "Internal Server Error";
				request.getSession().invalidate();
				break;
			case 404:
				if (uri.startsWith(DcemConstants.DEFAULT_WEB_NAME)) {
					
				//	if (module.isSwitchUserPortal() == false) {
						response.sendRedirect(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
								+ DcemConstants.DEFAULT_WEB_NAME + DcemConstants.WEB_MGT_CONTEXT);
						return;
				//	}
//					response.sendRedirect(
//							request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + DcemConstants.USER_PORTAL_WELCOME);
// TODO					return;
				}
				error = "Page Not Found";
				break;
			case 401:
				// request.getSession().invalidate();
				error = "Unauthorized";
				return;
			// break;
			case HttpServletResponse.SC_SERVICE_UNAVAILABLE:
				request.getSession().invalidate();
				error = "Service is unavailable";
				break;
			case 9000:
				request.getSession().invalidate();
				error = "Service is unavailablexxxxxxxxxxxxxxx";
				break;
			default:
				request.getSession().invalidate();
			}
		}

		response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		out.write("<html><head><title>DoubleClue Enterprise Management - Error Page</title></head><body>");

		out.write("<p /><br /><h2>DoubleClue Enterprise Management - Error Page</h2><p></p>");

		out.write("<p /><br /><h3>Oops.. Something went wrong. Please contact your administrator</h3><p></p>");

		out.write("<p />Request: " + uri + "<p />");
		out.write(error);

		// out.write("<ul><li>Servlet Name: " + servletName + "</li>");
		// out.write("<li>Status Code: " + statusCode + "</li>");
		// out.write("<li>Error: " + error + "</li>");
		// out.write("<li>Message: " + msg + "</li>");
		// out.write("<li>Requested URI :" + requestUri + "</li>");
		// if (throwable != null) {
		// out.write("<li>Exception Message:" + throwable.getMessage() + "</li>");
		// }
		// out.write("</ul>");

		out.write("</body></html>");
		out.close();
	}
}
