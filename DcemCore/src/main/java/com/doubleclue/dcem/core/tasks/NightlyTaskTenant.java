package com.doubleclue.dcem.core.tasks;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.weld.CdiUtils;

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
			System.out.println("NightlyTaskTenant.runTask()");
			logger.info("Nightly-Task for " + TenantIdResolver.getCurrentTenantName());
			OperatorSessionBean operatorSessionBean = CdiUtils.getReference(OperatorSessionBean.class);
			AdminModule adminModule =  CdiUtils.getReference(AdminModule.class);
			operatorSessionBean.setDcemUser(adminModule.getAdminTenantData().getSuperAdmin());
			for (DcemModule module : sortedModules) {
				if (module.isMasterOnly() && TenantIdResolver.isCurrentTenantMaster() == false) {
					continue;
				}
				module.runNightlyTask();
			}
		} catch (Exception e) {
			logger.error("NightlyTaskTenant",e);
		} finally {
			countDownLatch.countDown();
		}
	}

}
