package com.doubleclue.dcem.core.jpa;

import org.apache.logging.log4j.ThreadContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.TenantEntity;

public class TenantIdResolver implements CurrentTenantIdentifierResolver {

	static ThreadLocal<TenantEntity> currentTenant;
	static private TenantEntity masterTenant = null;

	@Override
	public String resolveCurrentTenantIdentifier() {
		return getCurrentTenantSchema();
	}

	@Override
	public boolean validateExistingCurrentSessions() {
		return false;
	}

	public static void setMasterTenant(TenantEntity masterTenant) {
		currentTenant = new ThreadLocal<>();
		TenantIdResolver.masterTenant = masterTenant;
		setCurrentTenant(masterTenant);
	}

	public static TenantEntity getMasterTenant() {
		return masterTenant;
	}
	
	public static void setMasterTenant() {
		currentTenant.set(masterTenant);
	}

	public static void setCurrentTenant(TenantEntity tenantEntity) {
		if (tenantEntity == null) {
			tenantEntity = masterTenant;
		}
		currentTenant.set(tenantEntity);
		if (tenantEntity.isMaster() == false) {
			ThreadContext.put(DcemConstants.MDC_TENANT, tenantEntity.getName());
		}
	}

	public static TenantEntity getCurrentTenant() {
		if (currentTenant == null || currentTenant.get() == null) {
			return masterTenant;
		}
		
		return currentTenant.get();
	}

	public static String getCurrentTenantSchema() {
		TenantEntity tenant = getCurrentTenant();
		if (tenant != null) {
			return tenant.getSchema();
		}
		return null;
	}

	public static String getCurrentTenantName() {
		TenantEntity tenant = getCurrentTenant();
		if (tenant != null) {
			return tenant.getName();
		}
		return null;
	}

	// public static Number getCurrentTenantId() {
	// TenantEntity tenant = getCurrentTenant();
	// if (tenant != null) {
	// return tenant.getId();
	// }
	// return -1;
	// }

	public static boolean isCurrentTenantMaster() {
		TenantEntity tenant = getCurrentTenant();
		if (tenant != null) {
			return tenant.isMaster();
		}
		return false; // TODO is this correct?
	}

}
