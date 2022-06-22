package com.doubleclue.dcem.core.licence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.AdminTenantData;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;

@ApplicationScoped
@Named ("licenceLogicCe")
public class LicenceLogicCe implements LicenceLogicInterface {

	private static final Logger logger = LogManager.getLogger(LicenceLogicCe.class);


	@Inject
	AdminModule adminModule;

	public void checkForLicence(AuthApplication application, boolean allowChanceAfterExpiration) throws DcemException {
	}

	public byte[] getEncryptedLicence(LicenceKeyContent licenceContent) throws DcemException {
		return null;
	}

	public LicenceKeyContent getDecryptedLicence(String licenceKey) throws DcemException {
		return null;
	}

	public LicenceKeyContent loadLicenceKeyContent() throws DcemException {
		AdminTenantData adminTenantData = adminModule.getAdminTenantData();
		LicenceKeyContent licenceKeyContent = new LicenceKeyContent();
		licenceKeyContent.setMaxUsers(1000000);
		licenceKeyContent.setExpiresOn(null);
		adminTenantData.setLicenceKeyContent(licenceKeyContent);
		return licenceKeyContent;
	}

	

	public void resetExpiredLicenceUserShouldAuthenticate() {
	}

	public String getLicencePolicy() {
		logger.info(DcemConstants.LICENCE_POLICY_COMMUNITY);
		return DcemConstants.LICENCE_POLICY_COMMUNITY;
	}

	public LicenceKeyContent createTrialLicence(int days, String customerName) throws DcemException {
		return null;
	}

	public String getEncryptedLicenceAsString(LicenceKeyContent licenceKeyContent) throws DcemException {
		return null;
	}

	public void addLicenceToDb(LicenceKeyContent licenceContent) throws DcemException {
	}

	public void setLicence(LicenceKeyContent licenceKeyContent, TenantEntity tenantEntity) throws Exception {
	}

	public void checkLicenceAlerts() {
	}

	public List<DcemReporting> getLicenceWarnings() {
		return new ArrayList<DcemReporting>();
		
	}

	public Map<String, List<DcemReporting>> getLicenceAlertsFromModules() {
		Map<String, List<DcemReporting>> licenceWarnings = new HashMap<>();
		return licenceWarnings;
	}

	public void sendLicenceWarningEmails() throws DcemException {
		
	}

	public LicenceKeyContent getLicenceKeyContent() {
		AdminTenantData adminTenantData = adminModule.getTenantData();
		return adminTenantData.getLicenceKeyContent();
	}

	public LicenceKeyContentUsage getTenantLicenceKeyUsage(TenantEntity tenantEntity) throws Exception {
		return null;
	}

	public LicenceKeyContentUsage getLicenceKeyContentUsage() {
	return null;
	}

	@Override
	public void reload() throws DcemException {
		
	}

}
