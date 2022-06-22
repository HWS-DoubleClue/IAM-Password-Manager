package com.doubleclue.dcem.userportal.servlets;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.servlets.DcemFilter;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcup.gui.PortalSessionBean;
import com.doubleclue.dcup.logic.DcupConstants;

public class EmbeddedUserPortalFilter extends DcemFilter {

//	private static final Logger logger = LogManager.getLogger(EmbeddedUserPortalFilter.class);
	private static final String LOGIN_URL = DcupConstants.WEB_USER_PORTAL_CONTEXT + "/login.xhtml";
	private static final String PRE_LOGIN_URL = DcupConstants.WEB_USER_PORTAL_CONTEXT + "/preLogin_.xhtml";
	


	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	UserPortalModule userPortalModule;
	
	@Inject
	PortalSessionBean portalSessionBean;

	// private final LoginUserPortalFilter filter = new LoginUserPortalFilter();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		super.init(filterConfig);

		webPort = DcemCluster.getInstance().getClusterConfig().getConnectionService(ConnectionServicesType.USER_PORTAL)
				.getPort();
		webName = DcupConstants.WEB_USER_PORTAL_CONTEXT;
		allowedPaths.add(LOGIN_URL);
		redirectionPage = PRE_LOGIN_URL;
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
		return portalSessionBean.isUserLoggedInAndEnabled();
	}

	@Override
	public String getUserId() {
		if (portalSessionBean.getDcemUser() != null) {
			return portalSessionBean.getDcemUser().getLoginId();
		}
		return null;
	}

//	@Override
//	protected void checkIfViewAllowed(HttpServletRequest httpServletRequest) throws Exception {
//		String servletPath = httpServletRequest.getServletPath();
//		if (servletPath.endsWith(DcupConstants.CLOUD_SAFE_VIEW) // ?????? contains() instead of endsWith() ??????
//				&& (userPortalModule.getModulePreferences().isViewVisible(ViewItem.CLOUD_DATA_VIEW) == false)) {
//			throw new DcemException(DcemErrorCodes.UNALLOWED_PATH, "message"); // here message
//		} else if (servletPath.endsWith(DcupConstants.PASSWORD_SAFE_VIEW)
//				&& (userPortalModule.getModulePreferences().isViewVisible(ViewItem.PASSWORD_MANAGER) == false)) {
//			throw new DcemException(DcemErrorCodes.UNALLOWED_PATH, "message");
//		} else if (servletPath.endsWith(DcupConstants.DEVICES_VIEW)
//				&& (userPortalModule.getModulePreferences().isViewVisible(ViewItem.NETWORK_DEVICE_VIEW) == false
//						&& userPortalModule.getModulePreferences().isViewVisible(ViewItem.FIDO_VIEW) == false						
//						&& userPortalModule.getModulePreferences().isViewVisible(ViewItem.OTP_TOKEN_VIEW) == false)) {
//			throw new DcemException(DcemErrorCodes.UNALLOWED_PATH, "message");
//		} else if (servletPath.endsWith(DcupConstants.USER_PROFILE_VIEW)
//				&& (userPortalModule.getModulePreferences().isViewVisible(ViewItem.USER_PROFILE_VIEW) == false)) {
//			
//			throw new DcemException(DcemErrorCodes.UNALLOWED_PATH, "message");
//		} else if (servletPath.endsWith(DcupConstants.CHANGE_PASSWORD_VIEW)
//				&& (userPortalModule.getModulePreferences().isViewVisible(ViewItem.CHANGE_PASSWORD_VIEW) == false)) {
//			
//			throw new DcemException(DcemErrorCodes.UNALLOWED_PATH, "message");
//		}
//		return;
//	}
}
