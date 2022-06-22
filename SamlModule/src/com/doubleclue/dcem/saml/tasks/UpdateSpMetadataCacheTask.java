package com.doubleclue.dcem.saml.tasks;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.tasks.CoreTask;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.saml.logic.SamlLogic;

@SuppressWarnings("serial")
public class UpdateSpMetadataCacheTask implements Runnable, Serializable {

	private static final Logger logger = LogManager.getLogger(CoreTask.class);

	private String entityId;
	private String tenantName;

	public UpdateSpMetadataCacheTask(String entityId, String tenantName) {
		super();
		this.entityId = entityId;
		this.tenantName = tenantName;
	}

	@Override
	public void run() {

		WeldRequestContext requestContext = null;

		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			requestContext = WeldContextUtils.activateRequestContext();
			SamlLogic samlLogic = CdiUtils.getReference(SamlLogic.class);
			DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
			TenantIdResolver.setCurrentTenant(applicationBean.getTenant(tenantName));
			samlLogic.invalidateMetadata(entityId);
		} catch (Throwable t) {
			logger.warn("Could not update SP Metadata Cache in task[" + Thread.currentThread().getName() + "]", t);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}
}
