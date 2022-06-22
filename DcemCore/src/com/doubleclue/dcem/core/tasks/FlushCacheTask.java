package com.doubleclue.dcem.core.tasks;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.JpaEntityCacheLogic;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

public class FlushCacheTask implements Callable<Exception>, Runnable, Serializable {

	private static final Logger logger = LogManager.getLogger(FlushCacheTask.class);
	private static final long serialVersionUID = 5291691247916289399L;

	static Object syncObject = new Object();

	String cacheName;
	String tenantName;

	public FlushCacheTask(String cacheName, String currentTenantName) {
		this.cacheName = cacheName;
		this.tenantName = currentTenantName;
	}

	@Override
	public Exception call() throws Exception {
		synchronized (syncObject) {
			if (DcemCluster.getInstance().isClusterMaster()) {
				Thread.currentThread().setName(this.getClass().getSimpleName());
				DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
				if (tenantName != null) {
					TenantEntity tenantEntity = applicationBean.getTenant(tenantName);
					flushCacheTenant(tenantEntity);
				} else {
					Collection<TenantEntity> tenantEntities = applicationBean.getTenantMap().values();
					for (TenantEntity tenantEntity : tenantEntities) {
						flushCacheTenant(tenantEntity);
					}
				}
			}
		}
		return null;
	}

	void flushCacheTenant(TenantEntity tenantEntity) {
		WeldRequestContext requestContext = null;
		TenantIdResolver.setCurrentTenant(tenantEntity);

		try {
			requestContext = WeldContextUtils.activateRequestContext();
			JpaEntityCacheLogic entityCacheLogic = CdiUtils.getReference(JpaEntityCacheLogic.class);
			if (cacheName != null|| cacheName == "null!master") {
				entityCacheLogic.flushCacheOnly(cacheName);
			} else {
				entityCacheLogic.flushAll();
			}
		} catch (Exception exp) {
			logger.error(exp);
			try {
				DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
				reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.FLUSH_CACHE_TO_DB,
						"CacheName: " + cacheName + "!" + tenantEntity.getName(), AlertSeverity.ERROR, false);
			} catch (Exception ex) {
				logger.error("Error while adding alert in flushCacheTenant", exp);
			}
		} finally {
			try {
				WeldContextUtils.deactivateRequestContext(requestContext);
			} catch (Exception exp) {
			}
		}
	}

	@Override
	public void run() {
		try {
			call();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
