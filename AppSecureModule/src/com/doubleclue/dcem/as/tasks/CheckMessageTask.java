package com.doubleclue.dcem.as.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.comm.AppSession;
import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.logic.AsTenantData;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.tasks.CoreTask;
import com.doubleclue.dcem.core.weld.CdiUtils;

public class CheckMessageTask extends CoreTask {

	private static final Logger logger = LogManager.getLogger(CheckMessageTask.class);

	
	AppSession appSession;
	AsTenantData tenantData;
	
	public CheckMessageTask(AppSession appSession, TenantEntity tenantEntity, AsTenantData tenantData) {
		super (CheckMessageTask.class.getSimpleName(), tenantEntity);
		this.appSession = appSession;
		this.tenantData = tenantData;
	}	


	@Override
	public void runTask() {
		try {
			AsMessageHandler asMessageHandler = CdiUtils.getReference(AsMessageHandler.class);
			asMessageHandler.checkPendingMessages(appSession, tenantData);
		} catch (Exception e) {
			logger.warn("", e);
			
		}
	}

}
