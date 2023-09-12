package com.doubleclue.dcem.core.tasks;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.weld.CdiUtils;

public class EmailTask extends CoreTask {

	private static final Logger logger = LogManager.getLogger(EmailTask.class);

	CountDownLatch countDownLatch;

	public EmailTask() {
		super(EmailTask.class.getSimpleName(), null);
	}

	@Override
	public void runTask() {

		if (DcemCluster.getDcemCluster().isClusterMaster() == false) {
			return;
		}
		Thread.currentThread().setName(this.getClass().getSimpleName());
		logger.info("Nightly run started");
		long start = System.currentTimeMillis();
		DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
		List<DcemModule> sortedModules = applicationBean.getSortedModules();
		TaskExecutor taskExecutor = CdiUtils.getReference(TaskExecutor.class);
		countDownLatch = new CountDownLatch(1);
		taskExecutor.execute(new NightlyTaskTenant(TenantIdResolver.getMasterTenant(), sortedModules, countDownLatch));
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			logger.warn(e);
		}
		Collection<TenantEntity> tenantEntities = applicationBean.getTenantMap().values();
		for (TenantEntity tenantEntity : tenantEntities) {
			if (tenantEntity.isMaster() == true) {
				continue;
			}
			countDownLatch = new CountDownLatch(1);
			taskExecutor.execute(new NightlyTaskTenant(tenantEntity, sortedModules, countDownLatch));
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
				logger.warn(e);
			}
		}
		logger.info("Nightly run ends: " + (System.currentTimeMillis() - start));
	}

}
