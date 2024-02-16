package com.doubleclue.dcem.core.tasks;

import java.io.Serializable;

import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

@SuppressWarnings("serial")
public class UpdateDbResources implements Runnable, Serializable {
	
	String tenantName;
	
	

	public UpdateDbResources(String tenantName) {
		super();
		this.tenantName = tenantName;
	}

	@Override
	public void run() {

		WeldRequestContext requestContext = null;

		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			requestContext = WeldContextUtils.activateRequestContext();
			DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
			TenantEntity tenantEntity = applicationBean.getTenant(tenantName);
			TenantIdResolver.setCurrentTenant(tenantEntity);
			DbResourceBundle.reloadResources();

		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}

}
