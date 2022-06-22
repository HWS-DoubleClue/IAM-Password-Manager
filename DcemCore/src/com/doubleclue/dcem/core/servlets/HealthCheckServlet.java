package com.doubleclue.dcem.core.servlets;

import java.io.IOException;
import java.util.Date;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;

@SuppressWarnings("serial")
@SessionScoped
public class HealthCheckServlet extends HttpServlet {

	private static final Logger logger = LogManager.getLogger(HealthCheckServlet.class);

	@Inject
	DcemApplicationBean applicationBean;

	int healthCheckPort;

	@Override
	public void init() throws ServletException {
		try {
			healthCheckPort = DcemCluster.getInstance().getClusterConfig().getConnectionService(ConnectionServicesType.HEALTH_CHECK).getPort();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e.getLocalizedMessage());
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("HealthCheck - Received Request via GET: " + request.getQueryString());
		}
		request(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("HealthCheck - Received Request via POST: " + request.getParameterMap().toString());
		}
		request(request, response);
	}

	private void request(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int remotePort = request.getLocalPort();
		if (remotePort != healthCheckPort) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}
		response.getOutputStream().write("OK\n".getBytes("UTF-8"));
		response.getOutputStream().write(new Date().toString().getBytes("UTF-8"));
	}

}
