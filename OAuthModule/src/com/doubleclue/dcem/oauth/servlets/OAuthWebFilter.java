package com.doubleclue.dcem.oauth.servlets;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.servlets.DcemFilter;
import com.doubleclue.dcem.oauth.logic.OAuthModuleConstants;
import com.doubleclue.oauth.utils.OAuthConstants;

public class OAuthWebFilter extends DcemFilter {

	private static final String PATH_SERVLET = "/" + OAuthModuleConstants.PATH_JSF_PAGES;
	private static final String PATH_LOGIN = PATH_SERVLET + "/" + OAuthModuleConstants.JSF_PAGE_LOGIN;
	private static final String PATH_RETURN = PATH_SERVLET + "/" + OAuthModuleConstants.JSF_PAGE_RETURN;
	private static final String PATH_USER_INFO = PATH_SERVLET + OAuthModuleConstants.URI_USER_INFO;
	private static final String PATH_JWKS = PATH_SERVLET + OAuthModuleConstants.URI_JWKS;
	private static final String PATH_SERVER_METADATA = PATH_SERVLET + OAuthConstants.URI_AUTH_SERVER_METADATA;
	private static final String PATH_OPENID_CONFIGURATION = PATH_SERVLET + OAuthConstants.URI_OPENID_CONFIGURATION;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);
		webPort = DcemCluster.getInstance().getClusterConfig().getConnectionService(ConnectionServicesType.OPENN_ID_OAUTH).getPort();
		webName = PATH_SERVLET;
		allowedPaths.add(PATH_SERVLET);
		allowedPaths.add(PATH_LOGIN);
		allowedPaths.add(PATH_RETURN);
		allowedPaths.add(PATH_USER_INFO);
		allowedPaths.add(PATH_JWKS);
		allowedPaths.add(PATH_SERVER_METADATA);
		allowedPaths.add(PATH_OPENID_CONFIGURATION);
		redirectionPage = PATH_SERVLET + "/" + DcemConstants.HTML_PAGE_PRE_LOGIN;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (((HttpServletRequest) request).getServletPath().equals(PATH_SERVLET)) { // if call to the servlet
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
