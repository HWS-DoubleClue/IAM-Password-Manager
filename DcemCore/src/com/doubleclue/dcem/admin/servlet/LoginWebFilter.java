package com.doubleclue.dcem.admin.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.ErrorDisplayBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.servlets.DcemFilter;

/**
 * @author galea
 * 
 */

// WebFilter(filterName = "LoginWebFilter", urlPatterns = "/mgt/*")
public class LoginWebFilter extends DcemFilter {

	private static final String PRE_LOGIN_URL = DcemConstants.WEB_MGT_CONTEXT + "/preLogin_.xhtml";
	private static final String LOGIN_URL = DcemConstants.WEB_MGT_CONTEXT + "/login.xhtml";

	// private static final Logger logger = LogManager.getLogger(LoginWebFilter.class);

	@Inject
	LocalConfigProvider localConfigProvider;

	@Inject
	ConfigLogic configLogic;

	@Inject
	OperatorSessionBean operatorSession;

	@Inject
	ErrorDisplayBean errorDisplayBean;

	@Inject
	DcemApplicationBean applicationBean;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		webPort = DcemCluster.getInstance().getClusterConfig().getConnectionService(ConnectionServicesType.MANAGEMENT).getPort();
		webName = DcemConstants.WEB_MGT_CONTEXT;
		allowedPaths.add(LOGIN_URL);
		redirectionPage = PRE_LOGIN_URL;
		welcomePage = DcemConstants.WELCOME_INDEX_PAGE;
	}

	@Override
	public boolean isLoggedin(HttpServletRequest httpServletRequest) {
		return operatorSession.isUserLoggedInAndEnabled(httpServletRequest);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (DcemApplicationBean.getInitExceptions().isEmpty() == false) {
			((HttpServletResponse) response).sendRedirect("/dcem/initErrors.xhtml");
			return;
		}
		((HttpServletResponse) response).addHeader("Strict-Transport-Security" ,"max-age=7776000");
	//	response  setHeader("Strict-Transport-Security" ,"max-age=7776000" );
		super.doFilter(request, response, chain);
	}
	
	@Override
	public void setTenant(TenantEntity tenantEntity) {
		TenantIdResolver.setCurrentTenant(tenantEntity);
	}

	@Override
	public String getUserId() {
		if (operatorSession.getDcemUser() != null) {
			return operatorSession.getDcemUser().getLoginId();
		}
		return null;
	}

	@Override
	public void logUserIn(DcemUser dcemUser, HttpServletRequest httpServletRequest) throws DcemException {
		operatorSession.loggedInOperator(dcemUser, httpServletRequest);
	}
}
