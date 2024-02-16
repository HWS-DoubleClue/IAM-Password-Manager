package com.doubleclue.dcem.as.tasks;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.comm.AppSession;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.AsTenantData;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.tasks.CoreTask;
import com.doubleclue.dcem.core.weld.CdiUtils;


public class KillDeviceTask extends CoreTask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(KillDeviceTask.class);

	int deviceId;

	public KillDeviceTask(int deviceId, TenantEntity tenantEntity) {
		super("KillDeviceTask", tenantEntity);
		this.deviceId = deviceId;
	}


	@Override
	public void runTask() {
		try {
			AsModule asModule = CdiUtils.getReference(AsModule.class);
			AsTenantData tenantData = asModule.getTenantData();
			AppSession appSession = tenantData.getDeviceSessions().get(deviceId);
			if (appSession != null) {
				appSession.getServerToApp().disconnect();
			}
			logger.debug("Device Disconnected: " + deviceId);
		} catch (Exception e) {
			logger.debug("Coundn't kill device " + deviceId, e);

		}
	}

}
