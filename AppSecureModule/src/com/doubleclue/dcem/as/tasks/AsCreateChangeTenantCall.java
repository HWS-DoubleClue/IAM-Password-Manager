package com.doubleclue.dcem.as.tasks;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.as.entities.ActivationCodeEntity;
import com.doubleclue.dcem.as.entities.AsVersionEntity;
import com.doubleclue.dcem.as.entities.PolicyEntity;
import com.doubleclue.dcem.as.logic.AsActivationLogic;
import com.doubleclue.dcem.as.logic.AsVersionLogic;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

public class AsCreateChangeTenantCall implements Callable<AsCreateChangeTenantCallResult> {

	private enum Purpose {
		CREATE_TENANT, RECOVER_SUPERADMIN_ACCESS, CREATE_ACTIVATION_CDOE
	}

	private static final Logger logger = LogManager.getLogger(AsCreateChangeTenantCall.class);

	private final TenantEntity tenantEntity;
	private PolicyEntity globalPolicy = null;
	private PolicyEntity mgtPolicy = null;
	private final Purpose purpose;
	String mobileNumber;
	boolean sendPasswordBySms;
	String superAdminPassword;
	SendByEnum activationCodeSendBy;
	List<AsVersionEntity> versions;
	DcemTemplate dcemTemplate;
	String emailAddress;
	String smsTextRewsource;
	String loginId;
	boolean selfCreateTenant;

	public AsCreateChangeTenantCall(TenantEntity tenantEntity, String emailAddress, String mobileNumber, SendByEnum activationCodeSendBy,
			DcemTemplate dcemTemplate, boolean sendPassBySms, String superAdminPassword, String smsTextRewsource, String loginId, boolean selfCreateTenant) {
		this.tenantEntity = tenantEntity;
		this.activationCodeSendBy = activationCodeSendBy;
		this.dcemTemplate = dcemTemplate;
		this.mobileNumber = mobileNumber;
		this.sendPasswordBySms = sendPassBySms;
		this.superAdminPassword = superAdminPassword;
		this.smsTextRewsource = smsTextRewsource;
		this.loginId = loginId;
		this.selfCreateTenant = selfCreateTenant;
		purpose = Purpose.CREATE_ACTIVATION_CDOE;
	}

	public AsCreateChangeTenantCall(TenantEntity tenantEntity) {
		this.tenantEntity = tenantEntity;
		purpose = Purpose.RECOVER_SUPERADMIN_ACCESS;
	}

	public AsCreateChangeTenantCall(TenantEntity tenantEntity, PolicyEntity globalPolicy, PolicyEntity mgtPolicy, List<AsVersionEntity> versions) {
		this.tenantEntity = tenantEntity;
		this.globalPolicy = globalPolicy;
		this.mgtPolicy = mgtPolicy;
		this.versions = versions;
		purpose = Purpose.CREATE_TENANT;
	}

	@Override
	public AsCreateChangeTenantCallResult call() {
		AsCreateChangeTenantCallResult result = new AsCreateChangeTenantCallResult();
		TenantIdResolver.setCurrentTenant(tenantEntity);
		PolicyLogic policyLogic = CdiUtils.getReference(PolicyLogic.class);
		AsVersionLogic versionLogic = CdiUtils.getReference(AsVersionLogic.class);
		WeldRequestContext requestContext = null;
		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			requestContext = WeldContextUtils.activateRequestContext();
			policyLogic.syncPolicyAppEntity();
			switch (purpose) {
			case CREATE_TENANT:
				policyLogic.setGlobalPolicy(globalPolicy, true);
				policyLogic.setManagementPolicy(mgtPolicy, true);
				versionLogic.addVersionsToTenant(versions);
				break;
			case CREATE_ACTIVATION_CDOE:
				try {
					if (selfCreateTenant == true) {
						result.setActivationCode(createActivationCodeForTenantLoginId());
					} else {
						result.setActivationCode(createActivationCodeForSuperAdmin());
					}
				} catch (DcemException e) {
					logger.warn("Error while creating Activation Code for SuperAdmin", e);
				}
				break;
			case RECOVER_SUPERADMIN_ACCESS:
				policyLogic.setBackdoorToManagementPolicy();
				policyLogic.reload(null);
				break;
			}
		} catch (Exception e) {
			logger.warn(e);
			result.setException(e);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
		return result;
	}

	private ActivationCodeEntity createActivationCodeForSuperAdmin() throws DcemException {
		UserLogic userLogic = CdiUtils.getReference(UserLogic.class);
		DcemUser superAdmin = userLogic.getDistinctUser(DcemConstants.SUPER_ADMIN_OPERATOR);
		if (superAdmin != null) {
			userLogic.setMailPasswordMobile(superAdmin, emailAddress, superAdminPassword, mobileNumber);
			AsActivationLogic activationLogic = CdiUtils.getReference(AsActivationLogic.class);
			ActivationCodeEntity activationCodeEntity = activationLogic.createActivationCode(superAdmin, null, activationCodeSendBy, null, dcemTemplate);
			if (sendPasswordBySms == true) {
				activationLogic.sendPasswordBySms(superAdmin, superAdminPassword, smsTextRewsource);
			}
			return activationCodeEntity;

		}
		throw new DcemException(DcemErrorCodes.INVALID_USERID, "SuperAdmin not found");
	}

	private ActivationCodeEntity createActivationCodeForTenantLoginId() throws DcemException {
		UserLogic userLogic = CdiUtils.getReference(UserLogic.class);
		DcemUser dcemUser = userLogic.getDistinctUser(loginId);
		if (loginId != null) {
			userLogic.setMailPasswordMobile(dcemUser, emailAddress, superAdminPassword, mobileNumber);
			AsActivationLogic activationLogic = CdiUtils.getReference(AsActivationLogic.class);
			ActivationCodeEntity activationCodeEntity = activationLogic.createActivationCode(dcemUser, null, activationCodeSendBy, null, dcemTemplate);
			if (sendPasswordBySms == true) {
				activationLogic.sendPasswordBySms(dcemUser, superAdminPassword, smsTextRewsource);
			}
			return activationCodeEntity;
		}
		throw new DcemException(DcemErrorCodes.INVALID_USERID, "dcemUser not found");
	}
}
