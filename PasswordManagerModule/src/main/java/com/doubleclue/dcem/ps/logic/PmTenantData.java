package com.doubleclue.dcem.ps.logic;

import java.util.List;

import com.doubleclue.dcem.core.logic.module.ModuleTenantData;
import com.doubleclue.dcem.ps.entities.ApplicationHubEntity;

public class PmTenantData extends ModuleTenantData {
	
	private List<ApplicationHubEntity> appHubEntities;

	public List<ApplicationHubEntity> getAppHubEntities() {
		return appHubEntities;
	}

	public void setAppHubEntities(List<ApplicationHubEntity> appHubEntities) {
		this.appHubEntities = appHubEntities;
	}
	
}