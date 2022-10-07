package com.doubleclue.dcem.saml.servlets;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.ErrorDisplayBean;
import com.doubleclue.dcem.core.jpa.EntityManagerProducer;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.servlets.DcemFilter;
import com.doubleclue.dcem.saml.logic.SamlConstants;

/**
 * @author galea
 * 
 */

// WebFilter(filterName = "LoginWebFilter", urlPatterns = "/saml/*")
public class SamlWebFilter extends DcemFilter {

	private static final String PATH_SERVLET = "/" + SamlConstants.PATH_JSF_PAGES;
	private static final String PATH_LOGIN = PATH_SERVLET + "/" + SamlConstants.JSF_PAGE_LOGIN;
	private static final String PRE_LOGIN = PATH_SERVLET + "/" + DcemConstants.HTML_PAGE_PRE_LOGIN;
	private static final String PATH_RETURN = PATH_SERVLET + "/" + SamlConstants.JSF_PAGE_RETURN_TO_SP;
	private static final String PATH_IDP_METADATA = PATH_SERVLET + "/" + SamlConstants.FILENAME_IDP_METADATA;
	private static final String PATH_TEST_SP = "/acs"; // for TestModule

	@Inject
	transient EntityManagerProducer emp;

	@Inject
	LocalConfigProvider localConfigProvider;

	@Inject
	ConfigLogic configLogic;

	@Inject
	ErrorDisplayBean errorDisplayBean;

	@Inject
	DcemApplicationBean applicationBean;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		webPort = DcemCluster.getInstance().getClusterConfig().getConnectionService(ConnectionServicesType.SAML).getPort();
		webName = PATH_SERVLET;
		allowedPaths.add(PATH_SERVLET);
		allowedPaths.add(PATH_LOGIN);
		allowedPaths.add(PATH_RETURN);
		allowedPaths.add(PATH_TEST_SP);
		allowedPaths.add(PATH_IDP_METADATA);
		redirectionPage = PRE_LOGIN;
	}

	@Override
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String path = ((HttpServletRequest) request).getServletPath();
		if (path.equals(PATH_SERVLET) || path.equals(PATH_IDP_METADATA)) { // if call to the servlet
			chain.doFilter(request, response);
		} else {
			super.doFilter(request, response, chain);
		}
	}

	@Override
	public boolean isLoggedin(HttpServletRequest httpServletRequest) {
		return false;
	}

	@Override
	public void setTenant(TenantEntity tenantEntity) {
		TenantIdResolver.setCurrentTenant(tenantEntity);
	}

	@Override
	public String getUserId() {
		return null;
	}

	@Override
	public void logUserIn(DcemUser dcmeUser, HttpServletRequest httpServletRequest) throws DcemException {
		throw new DcemException(DcemErrorCodes.NOT_IMPLEMENTED, null);
	}
}
