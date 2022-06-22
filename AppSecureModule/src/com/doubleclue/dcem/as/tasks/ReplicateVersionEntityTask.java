package com.doubleclue.dcem.as.tasks;

import java.io.Serializable;

import com.doubleclue.dcem.as.entities.AsVersionEntity;
import com.doubleclue.dcem.as.logic.AsVersionLogic;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

/**
 * 
 * This class is used from Cluster
 * @author Emanuel
 *
 */
public class ReplicateVersionEntityTask implements Runnable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	AsVersionEntity orgVersionEntity;
	AsVersionEntity modifiedVersionEntity;
	TenantEntity tenantEntity;

	public ReplicateVersionEntityTask(AsVersionEntity orgVersionEntity, AsVersionEntity modifiedVersionEntity, TenantEntity tenantEntity) {
		this.orgVersionEntity = orgVersionEntity;
		this.modifiedVersionEntity = modifiedVersionEntity;
		this.tenantEntity = tenantEntity;
	}

	@Override
	public void run() {
		WeldRequestContext requestContext = null;
		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			requestContext = WeldContextUtils.activateRequestContext();
			TenantIdResolver.setCurrentTenant(tenantEntity);
			AsVersionLogic asVersionLogic = CdiUtils.getReference(AsVersionLogic.class);
			asVersionLogic.replicateVersion(orgVersionEntity, modifiedVersionEntity);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}
}
