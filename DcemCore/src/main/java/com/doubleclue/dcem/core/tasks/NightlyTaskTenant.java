package com.doubleclue.dcem.core.tasks;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.module.DcemModule;

public class NightlyTaskTenant extends CoreTask {

	private static final Logger logger = LogManager.getLogger(NightlyTaskTenant.class);

	CountDownLatch countDownLatch;
	List<DcemModule> sortedModules;

	public NightlyTaskTenant(TenantEntity tenantEntity, List<DcemModule> sortedModules, CountDownLatch countDownLatch) {
		super (NightlyTaskTenant.class.getSimpleName(), tenantEntity);
		this.sortedModules = sortedModules;
		this.countDownLatch = countDownLatch;
	}

	@Override
	public void runTask() {
		try {
			logger.info("Nightly-Task for " + TenantIdResolver.getCurrentTenantName());
			for (DcemModule module : sortedModules) {
				if (module.isMasterOnly() && TenantIdResolver.isCurrentTenantMaster() == false) {
					continue;
				}
				module.runNightlyTask();
			}
		} catch (Exception e) {
			logger.error(e);
		} finally {
			countDownLatch.countDown();
		}
	}

}
