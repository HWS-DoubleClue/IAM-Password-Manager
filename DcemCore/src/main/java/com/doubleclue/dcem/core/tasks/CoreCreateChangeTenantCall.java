package com.doubleclue.dcem.core.tasks;

import java.util.TimeZone;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.CreateTenant;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

public class CoreCreateChangeTenantCall implements Callable<Exception> {

	private static final Logger logger = LogManager.getLogger(CoreCreateChangeTenantCall.class);

	private enum Purpose {
		CREATE_TENANT, RECOVER_SUPERADMIN_ACCESS
	}

	private TenantEntity tenantEntity;
	private String superAdminPassword;
	private String superAdminPhone;
	private String superAdminEmail;
	private Purpose purpose;
	private String loginId;
	private String displayName;
	private TimeZone timeZone;
	SupportedLanguage supportedLanguage;

	public CoreCreateChangeTenantCall(TenantEntity tenantEntity, String superAdminPassword, String superAdminPhone,
			String superAdminEmail, SupportedLanguage supportedLanguage, String loginId, String displayName, TimeZone timeZone) {
		this.tenantEntity = tenantEntity;
		this.superAdminPassword = superAdminPassword;
		this.superAdminPhone = superAdminPhone;
		this.superAdminEmail = superAdminEmail;
		this.supportedLanguage = supportedLanguage;
		this.loginId = loginId;
		this.displayName = displayName;
		this.timeZone = timeZone;
		this.purpose = Purpose.CREATE_TENANT;
	}

	public CoreCreateChangeTenantCall(TenantEntity tenantEntity, String superAdminPassword) {
		this.tenantEntity = tenantEntity;
		this.superAdminPassword = superAdminPassword;
		this.purpose = Purpose.RECOVER_SUPERADMIN_ACCESS;
	}

	@Override
	public Exception call() {
		TenantIdResolver.setCurrentTenant(tenantEntity);
		CreateTenant createTenant = CdiUtils.getReference(CreateTenant.class);
		WeldRequestContext requestContext = null;
		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			requestContext = WeldContextUtils.activateRequestContext();
			switch (purpose) {
			case CREATE_TENANT:
				createTenant.createTenant(tenantEntity, superAdminPassword, superAdminPhone, superAdminEmail, supportedLanguage, loginId,
						displayName, timeZone);
				break;
			case RECOVER_SUPERADMIN_ACCESS:
				createTenant.recoverSuperAdminAccess(superAdminPassword);
				break;
			}
			return null;
		} catch (Exception e) {
			logger.warn(e);
			return e;
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}
}
