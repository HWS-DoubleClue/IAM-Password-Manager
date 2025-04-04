package com.doubleclue.dcem.admin.preferences;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.validator.constraints.Range;

import com.doubleclue.dcem.admin.logic.NotificationType;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@XmlType
@XmlRootElement(name = "adminPreferences")
public class AdminPreferences extends ModulePreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@DcemGui
	@DecimalMin(value = "1", message = "Cannot be less than 1")
	@DecimalMax(value = "32", message = "Max value is 32")
	@Digits(fraction = 0, integer = 2, message = "Maximum 2 digits")
	@Range(min = 4, max = 32)
	int userPasswordLength = 4;

	@DcemGui(help = "Only applies to the Initial Password.")
	boolean numericPassword = true;

	@DcemGui(help = "Management Login Inactivity Timer in Minutes")
	@Min(1)
	@Max(120)
	int inactivityTimer = 30;

	@DcemGui()
	@Min(value = 2)
	@Max(value = 256)
	int passwordMaxRetryCounter = 5;

	@DcemGui()
	int suspendUserDuration = 60;

	// @DcemGui
	boolean saveUserPasswords = false;

	@DcemGui()
	boolean enableUserDomainSearch = false;

	@DcemGui()
	SupportedLanguage userDefaultLanguage = SupportedLanguage.English;

	@DcemGui()
	int durationForHistoryArchive = 356;

	@DcemGui()
	String defaultPhoneCountryCode = null;

	@DcemGui()
	String alertsNotificationGroup = null;

	@DcemGui(separator = "Login With")
	boolean enableAzureDirectLogin;

	@DcemGui()
	boolean useOnlyAzureDirectLogin;

	@DcemGui()
	boolean useWindowsSSO;
	
	@DcemGui(separator = "New User Registration")
	boolean enableCreateAccount;
	
	@DcemGui()
	NotificationType notificationType = NotificationType.BOTH;
	
	@DcemGui(help = "Activation code validation in minutes")
	int urlTokenTimeout = 120;  // minutes

	@DcemGui(separator = "Location", choose = { "None", "IP", "City" })
	String locationInformation = "None";

	@DcemGui(style = "width: 18em", password = true)
	String locationApiKey;

	@DcemGui(separator = "Reporting")
	boolean reportErrorsOnly = false;

	@DcemGui()
	@Min(10)
	@Max(value = 10000)
	int maxExport = 1000;

	@DcemGui()
	int durationForReportArchive = 0;

	public String getLocationInformation() {
		return locationInformation;
	}

	public void setLocationInformation(String locationInformation) {
		this.locationInformation = locationInformation;
	}

	public String getLocationApiKey() {
		return locationApiKey;
	}

	public void setLocationApiKey(String locationApiKey) {
		this.locationApiKey = locationApiKey;
	}

	public int getUserPasswordLength() {
		return userPasswordLength;
	}

	public void setUserPasswordLength(int userPasswordLength) {
		this.userPasswordLength = userPasswordLength;
	}

	public boolean isSaveUserPasswords() {
		return saveUserPasswords;
	}

	public void setSaveUserPasswords(boolean saveUserPasswords) {
		this.saveUserPasswords = saveUserPasswords;
	}

	public int getInactivityTimer() {
		return inactivityTimer;
	}

	public void setInactivityTimer(int inactivityTimer) {
		this.inactivityTimer = inactivityTimer;
	}

	public int getPasswordMaxRetryCounter() {
		return passwordMaxRetryCounter;
	}

	public void setPasswordMaxRetryCounter(int passwordMaxRetryCounter) {
		this.passwordMaxRetryCounter = passwordMaxRetryCounter;
	}

	public boolean isNumericPassword() {
		return numericPassword;
	}

	public void setNumericPassword(boolean numericPassword) {
		this.numericPassword = numericPassword;
	}

	public boolean isEnableUserDomainSearch() {
		return enableUserDomainSearch;
	}

	public void setEnableUserDomainSearch(boolean enableUserDomainSearch) {
		this.enableUserDomainSearch = enableUserDomainSearch;
	}

	public SupportedLanguage getUserDefaultLanguage() {
		return userDefaultLanguage;
	}

	public void setUserDefaultLanguage(SupportedLanguage userDefaultLanguage) {
		this.userDefaultLanguage = userDefaultLanguage;
	}

	public int getDurationForHistoryArchive() {
		return durationForHistoryArchive;
	}

	public void setDurationForHistoryArchive(int durationForHistoryArchive) {
		this.durationForHistoryArchive = durationForHistoryArchive;
	}

	public String getDefaultPhoneCountryCode() {
		return defaultPhoneCountryCode;
	}

	public void setDefaultPhoneCountryCode(String defaultPhoneCountryCode) {
		this.defaultPhoneCountryCode = defaultPhoneCountryCode;
	}

	public String getAlertsNotificationGroup() {
		return alertsNotificationGroup;
	}

	public void setAlertsNotificationGroup(String alertsNotificationGroup) {
		this.alertsNotificationGroup = alertsNotificationGroup;
	}

	public boolean isReportErrorsOnly() {
		return reportErrorsOnly;
	}

	public void setReportErrorsOnly(boolean reportErrorsOnly) {
		this.reportErrorsOnly = reportErrorsOnly;
	}

	public int getDurationForReportArchive() {
		return durationForReportArchive;
	}

	public void setDurationForReportArchive(int durationForReportArchive) {
		this.durationForReportArchive = durationForReportArchive;
	}

	public int getSuspendUserDuration() {
		return suspendUserDuration;
	}

	public void setSuspendUserDuration(int suspendUserDuration) {
		this.suspendUserDuration = suspendUserDuration;
	}

	public int getMaxExport() {
		return maxExport;
	}

	public boolean isEnableAzureDirectLogin() {
		return enableAzureDirectLogin;
	}

	public void setEnableAzureDirectLogin(boolean enableAzureDirectLogin) {
		this.enableAzureDirectLogin = enableAzureDirectLogin;
	}

	public void setMaxExport(int maxExport) {
		this.maxExport = maxExport;
	}

	public boolean isUseWindowsSSO() {
		return useWindowsSSO;
	}

	public void setUseWindowsSSO(boolean useWindowsSSO) {
		this.useWindowsSSO = useWindowsSSO;
	}

	public boolean isUseOnlyAzureDirectLogin() {
		return useOnlyAzureDirectLogin;
	}

	public void setUseOnlyAzureDirectLogin(boolean useOnlyAzureDirectLogin) {
		this.useOnlyAzureDirectLogin = useOnlyAzureDirectLogin;
	}

	public boolean isEnableCreateAccount() {
		return enableCreateAccount;
	}

	public void setEnableCreateAccount(boolean enableCreateAccount) {
		this.enableCreateAccount = enableCreateAccount;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public int getUrlTokenTimeout() {
		return urlTokenTimeout;
	}

	public void setUrlTokenTimeout(int urlTokenTimeout) {
		this.urlTokenTimeout = urlTokenTimeout;
	}

}
