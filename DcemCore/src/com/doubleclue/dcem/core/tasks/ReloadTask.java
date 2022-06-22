package com.doubleclue.dcem.core.tasks;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

@SuppressWarnings("serial")
public class ReloadTask implements Callable<Exception>, Serializable {

	String reloadClassInterface;
	String tenantName;

	public ReloadTask(String class1, String tenantName) {
		super();
		this.reloadClassInterface = class1;
		this.tenantName = tenantName;
	}

	private static final Logger logger = LogManager.getLogger(ReloadTask.class);

	@Override
	public Exception call () {

		WeldRequestContext requestContext = null;

		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			if (logger.isDebugEnabled()) {
				logger.debug(tenantName + ":" + reloadClassInterface);
			}
			requestContext = WeldContextUtils.activateRequestContext();
			DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
			TenantEntity tenantEntity = applicationBean.getTenant(tenantName);
			TenantIdResolver.setCurrentTenant(tenantEntity);
			ReloadClassInterface reloadClass = CdiUtils.getReference(reloadClassInterface);
			reloadClass.reload();
			
		} catch (Exception e) {
			return e;
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
		return null;
	}

}
