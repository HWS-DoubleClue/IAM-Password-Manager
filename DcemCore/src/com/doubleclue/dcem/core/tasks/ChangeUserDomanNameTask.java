package com.doubleclue.dcem.core.tasks;

import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

public class ChangeUserDomanNameTask implements Runnable {

	DomainEntity ldap;
	String previousName;
	String newName;
	TenantEntity tenantEntity;

	public ChangeUserDomanNameTask(DomainEntity ldap, String previousName, String newName, TenantEntity tenantEntity) {
		this.ldap = ldap;
		this.newName = newName;
		this.previousName = previousName;
		this.tenantEntity = tenantEntity;
	}

	@Override
	public void run() {
		boolean noMoreUsers = false;
		while (noMoreUsers == false) {
			noMoreUsers = updateUserBatch(ldap, previousName, newName, tenantEntity) == 0;
		}
	}

	private int updateUserBatch(DomainEntity ldap, String previousName, String newName, TenantEntity tenantEntity) {
		WeldRequestContext requestContext = null;
		TenantIdResolver.setCurrentTenant(tenantEntity);
		try {
			requestContext = WeldContextUtils.activateRequestContext();
			UserLogic userLogic = CdiUtils.getReference(UserLogic.class);
			return userLogic.replaceUsersLdapName(ldap, previousName, newName);
		} finally {
			try {
				WeldContextUtils.deactivateRequestContext(requestContext);
			} catch (Exception exp) {
			}
		}
	}

}
