package com.doubleclue.dcem.core.licence;

import java.util.List;
import java.util.Map;

import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;

public interface LicenceLogicInterface {

	void checkForLicence(AuthApplication application, boolean allowChanceAfterExpiration) throws DcemException;

	byte[] getEncryptedLicence(LicenceKeyContent licenceContent) throws DcemException;

	LicenceKeyContent getDecryptedLicence(String licenceKey) throws DcemException;

	LicenceKeyContent loadLicenceKeyContent() throws DcemException;

	void resetExpiredLicenceUserShouldAuthenticate();

	LicenceKeyContent createTrialLicence(int days, String customerName) throws DcemException;

	String getEncryptedLicenceAsString(LicenceKeyContent licenceKeyContent) throws DcemException;

	void addLicenceToDb(LicenceKeyContent licenceContent) throws DcemException;

	void setLicence(LicenceKeyContent licenceKeyContent, TenantEntity tenantEntity) throws Exception;

	void checkLicenceAlerts();

	List<DcemReporting> getLicenceWarnings();

	Map<String, List<DcemReporting>> getLicenceAlertsFromModules();

	void sendLicenceWarningEmails() throws DcemException;

	LicenceKeyContent getLicenceKeyContent();

	LicenceKeyContentUsage getTenantLicenceKeyUsage(TenantEntity tenantEntity) throws Exception;

	LicenceKeyContentUsage getLicenceKeyContentUsage();

	void reload() throws DcemException;
	
	String getLicencePolicy();

}