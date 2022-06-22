package com.doubleclue.dcem.core.tasks;

import java.io.Serializable;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.DiagnosticLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

@SuppressWarnings("serial")
public class MonitorClusterTask implements Runnable, Serializable {

	private static final Logger logger = LogManager.getLogger(MonitoringTask.class);

	Date date;

	public MonitorClusterTask(Date date) {
		super();
		this.date = date;
	}

	@Override
	public void run() {
		WeldRequestContext requestContext = null;
		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			TenantIdResolver.setCurrentTenant(null);   // master
			requestContext = WeldContextUtils.activateRequestContext();
			DiagnosticLogic monitorLogic = CdiUtils.getReference(DiagnosticLogic.class);
			monitorLogic.saveNodeStatistics(date);
		} catch (Throwable t) {
			logger.warn("Couldn't save statistics", t);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}
}
