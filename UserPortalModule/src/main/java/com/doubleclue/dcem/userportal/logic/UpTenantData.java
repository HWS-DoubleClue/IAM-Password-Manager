package com.doubleclue.dcem.userportal.logic;

import java.util.List;

import com.doubleclue.dcem.core.logic.module.ModuleTenantData;
import com.doubleclue.dcem.userportal.entities.ApplicationHubEntity;

public class UpTenantData extends ModuleTenantData {
	
	private List<ApplicationHubEntity> appHubEntities;

	public List<ApplicationHubEntity> getAppHubEntities() {
		return appHubEntities;
	}

	public void setAppHubEntities(List<ApplicationHubEntity> appHubEntities) {
		this.appHubEntities = appHubEntities;
	}
	
}