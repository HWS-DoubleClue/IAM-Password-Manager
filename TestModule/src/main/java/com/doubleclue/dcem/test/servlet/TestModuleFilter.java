package com.doubleclue.dcem.test.servlet;

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
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.servlets.DcemFilter;

public class TestModuleFilter extends DcemFilter {

	@Inject
	DcemApplicationBean applicationBean;


	// private final LoginUserPortalFilter filter = new LoginUserPortalFilter();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);

		webPort = DcemCluster.getInstance().getClusterConfig().getConnectionService(ConnectionServicesType.USER_PORTAL)
				.getPort();
		webName = "/" + DcemConstants.TESTMODULE_SERVLET_FILTER_NAME;
	//	allowedPaths.add("welcome.html");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		super.doFilter(request, response, chain);
	}

	@Override
	public void setTenant(TenantEntity tenantEntity) {
		TenantIdResolver.setCurrentTenant(tenantEntity);
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	@Override
	public boolean isLoggedin(HttpServletRequest httpServletRequest) {
		return true;
	}

	@Override
	public String getUserId() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void logUserIn(DcemUser dcmeUser, HttpServletRequest httpServletRequest) throws DcemException {
		throw new DcemException(DcemErrorCodes.NOT_IMPLEMENTED, null);
	}
}
