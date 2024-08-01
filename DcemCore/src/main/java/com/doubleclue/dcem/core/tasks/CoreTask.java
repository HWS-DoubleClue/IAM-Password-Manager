package com.doubleclue.dcem.core.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.core.weld.WeldSessionContext;

/**
 * 
 * 
 * @author Emanuel Galea
 *
 */
public abstract class CoreTask implements Runnable {

	protected static final Logger logger = LogManager.getLogger(CoreTask.class);

	private String name;
	private TenantEntity coreTenantEntity;

	public CoreTask(String name) {
		this.name = name;
		coreTenantEntity = TenantIdResolver.getCurrentTenant();
	}
	
	public CoreTask(String name, TenantEntity tenantEntity) {
		this.name = name;
		coreTenantEntity = tenantEntity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		WeldRequestContext requestContext = null;
		WeldSessionContext sessionContext = null;
		try {
			Thread.currentThread().setName(name);
			TenantIdResolver.setCurrentTenant(coreTenantEntity);
			requestContext = WeldContextUtils.activateRequestContext();
			sessionContext = WeldContextUtils.activateSessionContext(null);

			if (logger.isTraceEnabled()) {
				logger.trace("trying to execute SEM task[" + Thread.currentThread().getName() + "]");
			}
			runTask();
			if (logger.isTraceEnabled()) {
				logger.trace("SEM task[" + Thread.currentThread().getName() + "] executed successfully.");
			}
		} catch (Throwable t) {
			logger.warn("Could not execute SEM task[" + Thread.currentThread().getName() + "]", t);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
			WeldContextUtils.deactivateSessionContext(sessionContext);
		}
	}

	public abstract void runTask();

}
