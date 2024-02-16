package com.doubleclue.dcem.core.weld;

import java.util.Map;

import org.jboss.weld.context.bound.BoundRequestContext;

/**
 * @author Emanuel Galea
 *
 */
public class WeldRequestContext {
	
	private BoundRequestContext requestContext;
	
	private Map<String, Object> requestDataStore;

	public WeldRequestContext(BoundRequestContext requestContext, Map<String, Object> requestDataStore) {
		this.requestContext = requestContext;
		this.requestDataStore = requestDataStore;
	}

	public BoundRequestContext getRequestContext() {
		return requestContext;
	}

	public void setRequestContext(BoundRequestContext requestContext) {
		this.requestContext = requestContext;
	}

	public Map<String, Object> getRequestDataStore() {
		return requestDataStore;
	}

	public void setRequestDataStore(Map<String, Object> requestDataStore) {
		this.requestDataStore = requestDataStore;
	}
	
}
