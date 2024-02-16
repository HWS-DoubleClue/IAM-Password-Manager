package com.doubleclue.dcem.test.logic;

import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.core.weld.WeldSessionContext;

public class UnitTask implements Callable<Exception> {
	
	private static final Logger logger = LogManager.getLogger(UnitTask.class);
	
	AbstractTestUnit testUnit = null;
	TenantEntity tenantEntity; 

	public UnitTask(AbstractTestUnit testUnit) {
		this.testUnit = testUnit;
		this.tenantEntity = TenantIdResolver.getCurrentTenant();
	}
	
	@Override
	public Exception call() {
		TenantIdResolver.setCurrentTenant(tenantEntity);
		// TODO @Emanuel: this line throws an error when trying to execute any number of tests
	//	TestExecutor testExecutor = CdiUtils.getReference(TestExecutor.class);
		WeldSessionContext sessionContext = null;
		WeldRequestContext requestContext = null;
		try {
			sessionContext = WeldContextUtils.activateSessionContext(null);
			requestContext = WeldContextUtils.activateRequestContext();
			testUnit.setTestStatus(TestStatus.Running);		
		//	testExecutor.setRunningTestUnit(testUnit);		
			testUnit.setDate(new Date());
			testUnit.start();
			testUnit.setTestStatus(TestStatus.Passed);
		//	testExecutor.setRunningTestUnit(null);
		} catch (Exception e) {
			if (testUnit != null) {
				testUnit.setTestStatus(TestStatus.Error);
				testUnit.setInfo(e.toString());
//				testExecutor.setErrorOcurred(true);
//				testExecutor.setRunningTestUnit(null);
			}
			logger.warn("", e);
			return e;
		} finally {
			WeldContextUtils.deactivateSessionContext(sessionContext);
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
		return null;
	}
}
