package com.doubleclue.dcem.as.policy;

import com.doubleclue.dcem.as.entities.AppPolicyGroupEntity;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;

public class PolicyTreeItem {

	AppPolicyGroupEntity policyGroupEntity;
	PolicyAppEntity appEntity;

	public PolicyTreeItem() {

	}

	public PolicyTreeItem(PolicyAppEntity appEntity, AppPolicyGroupEntity policyGroupEntity) {
		super();
		this.policyGroupEntity = policyGroupEntity;
		this.appEntity = appEntity;
	}

	public String getPolicyName() {
		if (policyGroupEntity != null) {
			if (policyGroupEntity.getPolicyEntity() != null) {
				return policyGroupEntity.getPolicyEntity().getName();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public String getPolicyContent() {
		if (policyGroupEntity != null) {
			if (policyGroupEntity.getPolicyEntity() != null) {
				return policyGroupEntity.getPolicyEntity().getJsonPolicy();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public boolean isGroup() {
		if (policyGroupEntity != null && policyGroupEntity.getGroup() != null) {
			return true;
		}
		return false;
	}

	public String getName() {
		if (policyGroupEntity != null && policyGroupEntity.getGroup() != null) {
			return policyGroupEntity.getGroup().getName() + " - " + policyGroupEntity.getPriority();
		}
		if (appEntity.getSubName() != null) {
			return appEntity.getSubName();
		}
		return appEntity.toString();
	}
	
	
	public AppPolicyGroupEntity getPolicyGroupEntity() {
		return policyGroupEntity;
	}

	public void setPolicyGroupEntity(AppPolicyGroupEntity policyGroupEntity) {
		this.policyGroupEntity = policyGroupEntity;
	}

	public PolicyAppEntity getAppEntity() {
		return appEntity;
	}

	public void setAppEntity(PolicyAppEntity appEntity) {
		this.appEntity = appEntity;
	}

}
