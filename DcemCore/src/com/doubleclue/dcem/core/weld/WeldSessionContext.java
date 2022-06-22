package com.doubleclue.dcem.core.weld;

import java.util.Map;

import org.jboss.weld.context.bound.BoundSessionContext;

/**
 * @author Emanuel Galea
 *
 */
public class WeldSessionContext {
	
	BoundSessionContext sessionContext;
	
	Map<String, Object> sessionDataStore;
	
	public WeldSessionContext(BoundSessionContext sessionContext, Map<String, Object> sessionDataStore) {
		this.sessionContext = sessionContext;
		this.sessionDataStore = sessionDataStore;
	}

	/**
	 * @return the sessionContext
	 */
	public BoundSessionContext getSessionContext() {
		return sessionContext;
	}

	/**
	 * @return the sessionDataStore
	 */
	public Map<String, Object> getSessionDataStore() {
		return sessionDataStore;
	}
}
