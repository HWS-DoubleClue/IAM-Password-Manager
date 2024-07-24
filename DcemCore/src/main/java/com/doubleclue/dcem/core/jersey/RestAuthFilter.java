package com.doubleclue.dcem.core.jersey;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.LoginAuthenticator;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Jersey HTTP Basic Auth filter
 * @author Emanuel Galea
 */
@Provider
@PreMatching
public class RestAuthFilter implements ContainerRequestFilter {

	private static final Logger logger = LogManager.getLogger(RestAuthFilter.class);
	
	static int SESSOIN_TIMEOUT_ON_ERROR = 2;

	static LoadingCache<String, DcemUser> cache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.MINUTES)
			.build(new CacheLoader<String, DcemUser>() {
				@Override
				public DcemUser load(String key) throws Exception {
					return new DcemUser(0);
				}
			});

	static Object syncObject = new Object();

	static int restPort = DcemCluster.getInstance().getClusterConfig().getConnectionService(ConnectionServicesType.REST).getPort();
	
	static String embeddedPass;

	@Context
	private HttpServletRequest servletRequest;

	/**
	 * Apply the filter : check input request, validate or not with user auth
	 * @param containerRequest The request from Tomcat server
	 */
	@Override
	public void filter(ContainerRequestContext containerRequest) throws WebApplicationException {

		// GET, POST, PUT, DELETE, ...
		String method = containerRequest.getMethod();
		String path = containerRequest.getUriInfo().getPath(true);
		if (servletRequest.getLocalPort() != restPort) {
			logger.warn("REST service with wrong Port, from " + servletRequest.getRemoteHost());
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("Invalid URI").build());
		}

		// System.out.println(path);
		long startTime = System.currentTimeMillis();
		// We do allow wadl to be retrieve
		if (method.equals("GET") && (path.equals("application.wadl") || path.equals("application.wadl/xsd0.xsd"))) {
			return;
		}

		// Map<String, Cookie> map = containerRequest.getCookies();
		// System.out.println("RestAuthFilter.filter() Path=" + containerRequest.getUriInfo().getRequestUri().getPath()
		// + ", ID=" + servletRequest.getSession().getId());

		// Get the authentification passed in HTTP headers parameters
		String auth = containerRequest.getHeaderString("authorization");
		String uri = containerRequest.getUriInfo().getRequestUri().getPath();
		int ind = uri.indexOf("restApi/");
		if (ind == -1) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("Invalid URI").build());
		}
		ind += 8;
		int ind2 = uri.indexOf("/", ind);
		if (ind2 == -1) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("Invalid URI").build());
		}
		OperatorSessionBean operatorSession = CdiUtils.getReference(OperatorSessionBean.class);
		containerRequest.setProperty("startTime", startTime);
		if (operatorSession.isUserLoggedInAndEnabled()) {
			// ready
			TenantIdResolver.setCurrentTenant(operatorSession.getTenantEntity());
			return;
		}
		if (auth == null) {
			logger.info("REST-API missing HttpBasicAuthentication");
			servletRequest.getSession().setMaxInactiveInterval(SESSOIN_TIMEOUT_ON_ERROR);
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("HttpBasic Authentication required!").build());
		}
		DcemApplicationBean dcemApplicationBean = CdiUtils.getReference(DcemApplicationBean.class);
		servletRequest.getSession().setMaxInactiveInterval(60 * 5);  // 5 minutes
		DcemUser dcemUser = null;
		try {
			dcemUser = cache.get(auth);
			if (dcemUser != null && dcemUser.getId() > 0) {
				TenantEntity tenantEntity = dcemApplicationBean.getTenant(dcemUser.getTenantName());
				if (tenantEntity != null) {
					operatorSession.setDcemUser(dcemUser);
					operatorSession.setLoggedIn(true);
					operatorSession.setTenantEntity(tenantEntity);
					TenantIdResolver.setCurrentTenant(tenantEntity);
					return;
				}
				cache.invalidate(auth);
			}
		} catch (Exception e1) {
			logger.info("RestAuthFilter.filter()", e1);
		}

		LoginAuthenticator loginAuthenticator = null;
		try {
			loginAuthenticator = DcemUtils.getHttpBasicAuthentication(auth);
		} catch (Exception e) {
			logger.info("REST-API incorrect HttpBasicAuthentication");
			servletRequest.getSession().setMaxInactiveInterval(5);
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("Invalid Authentication!").build());
		}

		// If the user does not have the right (does not provide any HTTP Basic Auth)
		if (loginAuthenticator == null) {
			logger.info("REST-API without authentication");
			servletRequest.getSession().setMaxInactiveInterval(SESSOIN_TIMEOUT_ON_ERROR);
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("Invalid Authentication!").build());
		}

		try {
			logger.info("REST Login");
			boolean verifiedOperator = false;
			synchronized (syncObject) {
				verifiedOperator = operatorSession.restLogin(loginAuthenticator);
			}
			if (verifiedOperator == false) {
				logger.info("Operator login for REST-API failed: " + loginAuthenticator.getName());
				servletRequest.getSession().setMaxInactiveInterval(SESSOIN_TIMEOUT_ON_ERROR);
				throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("Please check your authentication!").build());
			}
		} catch (DcemException e) {
			logger.info (e);
			servletRequest.getSession().setMaxInactiveInterval(SESSOIN_TIMEOUT_ON_ERROR);
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity(e.getErrorCode().name()).build());
		} 
		dcemUser = operatorSession.getDcemUser();
		dcemUser.setTenantName(operatorSession.getTenantEntity().getName());
		
		String moduleId = path.substring(0, path.indexOf('/'));
		DcemModule module = dcemApplicationBean.getModule(moduleId);
		DcemAction dcemAction = new DcemAction(module.getId(), DcemConstants.EMPTY_SUBJECT_NAME, DcemConstants.ACTION_REST_API);
		if (operatorSession.isPermission(dcemAction) == false) {
			logger.info("Operator has no rights for REST-API: " + dcemUser.getLoginId());
			servletRequest.getSession().setMaxInactiveInterval(SESSOIN_TIMEOUT_ON_ERROR);
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("Operator has no API rights for this Module").build());
		}
		cache.put(auth, dcemUser);
		TenantIdResolver.setCurrentTenant(operatorSession.getTenantEntity());
		module.addCounter("REST-Login", (System.currentTimeMillis() - startTime));
	}

	public static String getEmbeddedPass() {
		return embeddedPass;
	}

	public static void setEmbeddedPass(String embeddedPass) {
		RestAuthFilter.embeddedPass = embeddedPass;
	}

}