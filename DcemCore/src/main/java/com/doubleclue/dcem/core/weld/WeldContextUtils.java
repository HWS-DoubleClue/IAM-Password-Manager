package com.doubleclue.dcem.core.weld;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.jboss.weld.context.bound.BoundSessionContext;
import org.jboss.weld.context.http.HttpRequestContext;
import org.jboss.weld.context.http.HttpSessionContext;


/**
 * @author Emanuel Galea
 */
public class WeldContextUtils {

	private static final Logger logger = LogManager.getLogger(WeldContextUtils.class);

	private static final String UNABLE_TO_INJECT_HTTP_REQUEST_CONTEXT = "Unable to inject http request context";
	private static final String UNABLE_TO_INJECT_BOUND_REQUEST_CONTEXT = "Unable to inject bound request context";
	private static final String HTTP_REQUEST_CONTEXT_IS_STILL_ACTIVE = "Http request context is still active";
	private static final String BOUND_REQUEST_CONTEXT_IS_STILL_ACTIVE = "Bound request context is still active";
	private static final String ACTIVATE_REQUEST_CONTEXT = "Activate request context";
	private static final String NO_REQUEST_CONTEXT_TO_DEACTIVATE = "No request context to deactivate";
	private static final String DEACTIVATE_BOUND_REQUEST_CONTEXT = "Deactivate bound request context";
	
	private static final String UNABLE_TO_INJECT_HTTP_SESSION_CONTEXT = "Unable to inject http session context";
	private static final String UNABLE_TO_INJECT_BOUND_SESSION_CONTEXT = "Unable to inject bound session context";
	private static final String HTTP_SESSION_CONTEXT_IS_STILL_ACTIVE = "Http session context is still active";
	private static final String BOUND_SESSION_CONTEXT_IS_STILL_ACTIVE = "Bound session context is still active";
	private static final String ACTIVATE_SESSION_CONTEXT = "Activate session context";
	private static final String NO_SESSION_CONTEXT_TO_DEACTIVATE = "No session context to deactivate";
	private static final String DEACTIVATE_BOUND_SESSION_CONTEXT = "Deactivate bound session context";

	public static WeldRequestContext activateRequestContext() {

		HttpRequestContext requestContext = CdiUtils.getReference(HttpRequestContext.class);
		if (requestContext != null) {
			if (requestContext.isActive() == false) {

				BoundRequestContext boundRequestContext = CdiUtils.getReference(BoundRequestContext.class);
				if (boundRequestContext != null) {
					if (boundRequestContext.isActive() == false) {

						logger.trace(ACTIVATE_REQUEST_CONTEXT);

						Map<String, Object> requestDataStore = new HashMap<String, Object>();

						boundRequestContext.associate(requestDataStore);
						boundRequestContext.activate();

						// Starting WELD Request Scope
						return new WeldRequestContext(boundRequestContext, requestDataStore);
					} else {
						logger.debug(BOUND_REQUEST_CONTEXT_IS_STILL_ACTIVE);
					}
				} else {
					logger.error(UNABLE_TO_INJECT_BOUND_REQUEST_CONTEXT);
					throw new RuntimeException(UNABLE_TO_INJECT_BOUND_REQUEST_CONTEXT);
				}
			} else {
				logger.debug(HTTP_REQUEST_CONTEXT_IS_STILL_ACTIVE);
			}
		} else {
			logger.error(UNABLE_TO_INJECT_HTTP_REQUEST_CONTEXT);
			throw new RuntimeException(UNABLE_TO_INJECT_HTTP_REQUEST_CONTEXT);
		}

		return null;
	}

	public static void deactivateHttpContext(HttpRequestContext requestContext, HttpServletRequest servletRequest) {

		if (requestContext != null) {

			try {
				requestContext.invalidate();
				requestContext.deactivate();
			} finally {
				if (servletRequest != null) {
					requestContext.dissociate(servletRequest);
				}
			}
		}
	}
	
	public static void deactivateRequestContext(final WeldRequestContext weldRequestContext) {

		if(weldRequestContext == null) {
			logger.trace(NO_REQUEST_CONTEXT_TO_DEACTIVATE);
			return;
		}

		BoundRequestContext boundRequestContext = weldRequestContext.getRequestContext();
		try {
			// Stopping WELD Request Scope
			if (boundRequestContext != null) {
				
				logger.trace(DEACTIVATE_BOUND_REQUEST_CONTEXT);
				
				/* Invalidate the request (all bean instances will be scheduled for destruction) */
				boundRequestContext.invalidate();
				/* Deactivate the request, causing all bean instances to be destroyed (as the context is invalid) */
				boundRequestContext.deactivate();
			}
		} finally {
			/* Ensure that whatever happens we dissociate to prevent any memory leaks */
			if (boundRequestContext != null) {
				boundRequestContext.dissociate(weldRequestContext.getRequestDataStore());
			}
		}
	}

	public static WeldSessionContext activateSessionContext(Map<String, Object> sessionStorage) {

		HttpSessionContext sessionContext = CdiUtils.getReference(HttpSessionContext.class);
		if (sessionContext != null) {
			if (sessionContext.isActive() == false) {

				BoundSessionContext boundSessionContext = CdiUtils.getReference(BoundSessionContext.class);
				if (boundSessionContext != null) {
					if (boundSessionContext.isActive() == false) {

						logger.trace(ACTIVATE_SESSION_CONTEXT);
						if (sessionStorage == null) {
							sessionStorage = new HashMap<String, Object>();
						}

						boundSessionContext.associate(sessionStorage);
						boundSessionContext.activate();

						// Starting WELD Request Scope
						return new WeldSessionContext(boundSessionContext, sessionStorage);
					} else {
						logger.debug(BOUND_SESSION_CONTEXT_IS_STILL_ACTIVE);
					}
				} else {
					logger.error(UNABLE_TO_INJECT_BOUND_SESSION_CONTEXT);
					throw new RuntimeException(UNABLE_TO_INJECT_BOUND_SESSION_CONTEXT);
				}
			} else {
				logger.debug(HTTP_SESSION_CONTEXT_IS_STILL_ACTIVE);
			}
		} else {
			logger.error(UNABLE_TO_INJECT_HTTP_SESSION_CONTEXT);
			throw new RuntimeException(UNABLE_TO_INJECT_HTTP_SESSION_CONTEXT);
		}

		return null;
		
	}

	public static void deactivateSessionContext(final WeldSessionContext weldSessionContext) {
		
		if(weldSessionContext == null) {
			logger.trace(NO_SESSION_CONTEXT_TO_DEACTIVATE);
			return;
		}
		
		BoundSessionContext sessionContext = weldSessionContext.getSessionContext();
		try {
			// Stopping WELD Session Scope
			if (sessionContext != null) {
				logger.trace(DEACTIVATE_BOUND_SESSION_CONTEXT);

				/* Invalidate the Session (all bean instances will be scheduled for destruction) */
				sessionContext.invalidate();
				/* Deactivate the Session, causing all bean instances to be destroyed (as the context is invalid) */
				sessionContext.deactivate();
			}
		} finally {
			/* Ensure that whatever happens we dissociate to prevent any memory leaks */
			if (sessionContext != null) {
				sessionContext.dissociate(weldSessionContext.getSessionDataStore());
			}
		}

	}
}
