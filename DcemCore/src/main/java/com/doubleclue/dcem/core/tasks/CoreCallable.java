package com.doubleclue.dcem.core.tasks;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

/**
 * 
 * 
 * @author Emanuel Galea
 *
 */
public abstract class CoreCallable implements Callable<Object> {

	protected static final Logger logger = LogManager.getLogger(CoreCallable.class);

	private String name;
	private TenantEntity coreTenantEntity;

	public CoreCallable() {
		this.name = "";
		coreTenantEntity = TenantIdResolver.getCurrentTenant();
	}
	
	public CoreCallable(String name, TenantEntity tenantEntity) {
		this.name = name;
		coreTenantEntity = tenantEntity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public Object call() {
		WeldRequestContext requestContext = null;

		try {
			Thread.currentThread().setName(name);
			TenantIdResolver.setCurrentTenant(coreTenantEntity);
			requestContext = WeldContextUtils.activateRequestContext();
			return runCallable();
		} catch (Exception exp) {
			logger.warn("Could not execute SEM task[" + Thread.currentThread().getName() + "]", exp);
			return exp;
		} catch (Throwable t) {
			logger.warn("Could not execute SEM task[" + Thread.currentThread().getName() + "]", t);
			return new Exception(t);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	
	}

	public abstract Object runCallable();

}
