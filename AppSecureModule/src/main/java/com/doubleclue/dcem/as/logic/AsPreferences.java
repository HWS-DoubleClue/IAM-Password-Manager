package com.doubleclue.dcem.as.logic;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.core.utils.DisplayModes;

@SuppressWarnings("serial")
@XmlType
@XmlRootElement(name = "AsPreferences")
public class AsPreferences extends ModulePreferences {

	@DecimalMin(value = "10", message = "Cannot be less than 10 seconds")
	@DecimalMax(value = "600", message = "Max value is 600")
	@Digits(fraction = 0, integer = 6, message = "Maximum 4 digits")
	@DcemGui(help = "in Seconds")
	int loginQrCodeResponseTimeout = 4 * 60;

	@DecimalMin(value = "120", message = "Cannot be less than 2 Minutes")
	@DecimalMax(value = "999999", message = "Max value is 9999")
	@Digits(fraction = 0, integer = 6, message = "Maximum 6 digits")
	@DcemGui(help = "in Seconds")
	int keepAliveConnection = 10 * 60;
	
	@DecimalMin(value = "0", message = "Cannot be less than zero")
	@DecimalMax(value = "999999", message = "Max value is 99999")
	@Digits(fraction = 0, integer = 6, message = "Maximum 6 digits")
	@DcemGui(name="App Re-Login Within",help = "After a disconnection, the app can make a silent relogin within this time in minutes")
	int appReLoginWithin = 60*24;

	@DecimalMin(value = "4", message = "Cannot be less than 4")
	@DecimalMax(value = "32", message = "Max value is 32")
	@Digits(fraction = 0, integer = 2, message = "Maximum 2 digits")
	@DcemGui(separator = "Activation Code Configuration")
	int activationCodeLength = 10;

	@DcemGui
	boolean numericActivationCode = false;

	@DecimalMin(value = "1", message = "Cannot be less than 1")
	@DecimalMax(value = "9999", message = "Max value is 9999")
	@Digits(fraction = 0, integer = 4, message = "Maximum 4 digits")
	@DcemGui(help = "in Hours")
	int activationCodeDefaultValidTill = 24 * 7;

	@DecimalMin(value = "1", message = "Cannot be less than 1")
	@DecimalMax(value = "9999", message = "Max value is 9999")
	@Digits(fraction = 0, integer = 4, message = "Maximum 4 digits")
	@DcemGui(help = "in Minutes")
	int requestActivationCodeValidTill = 30;

	@DecimalMin(value = "1", message = "Cannot be less than 1")
	@DecimalMax(value = "9999", message = "Max value is 9999")
	@Digits(fraction = 0, integer = 2, message = "Maximum 4 digits")
	@DcemGui
	int maxFailActivations = 5;

	@DecimalMin(value = "1", message = "Cannot be less than 1")
	@DecimalMax(value = "9999", message = "Max value is 9999")
	@Digits(fraction = 0, integer = 4, message = "Maximum 4 digits")
	@DcemGui(help = "in Minutes")
	int retryActivationDelayinMinutes = 30;

	@DcemGui(help = "Passcode is valid for xx minutes.")
	@Min(value = 1)
	@Max(value = 10)
	int passcodeValidFor = 1;
	
	@DcemGui(help = "Will try to verify the Passcode plus/minus 'Passcode Valid For'.")
	@Min(value = 1)
	@Max(value = 10)
	int passcodeWindow = 1;

	@DecimalMin(value = "10", message = "Cannot be less than 10 seconds")
	@DecimalMax(value = "600", message = "Max value is 600")
	@Digits(fraction = 0, integer = 6, message = "Maximum 6 digits")
	@DcemGui(separator = "Push Approval Configuration", help = "in Seconds")
	int messageResponseTimeout = 5 * 60;

	@DcemGui // (choose = { "None", "To Device", "From Device", "All" })
	MsgStoragePolicy messageStorePolicy = MsgStoragePolicy.Dont_Store;

	@DcemGui(help = "in Seconds")
	@DecimalMin(value = "2", message = "Cannot be less than 2 seconds")
	@DecimalMax(value = "999", message = "Max value is 999 seconds ")
	@Digits(fraction = 0, integer = 6, message = "Maximum 2 digits")
	int messageRetrieveTimeoutSec = 120;

	@DecimalMin(value = "1", message = "Cannot be less than 1")
	@DecimalMax(value = "99", message = "Max value is 99")
	@Digits(fraction = 0, integer = 2, message = "Maximum 2 digits")
	@DcemGui
	int maxMessageQueueLength = 10;

	@DcemGui(help = "If set, all messages with the status 'queued' will be removed from the queue when a new message is received.")
	boolean clearQueuedMsgsOnNewMsg = false;

	@DcemGui
	String defaultTemplate = "as.Login";

	@DcemGui(separator = "Other", masterOnly = true)
	boolean enableAppAutoVersionRegistration = true;

	@DcemGui(separator = "DoubleClue Dispatcher REALM Name", displayMode = DisplayModes.INPUT_DISABLED)
	String realmName = null;
	
	@DcemGui(separator = "Archive - Archiving is executed on the 1st day of each month", help = "days. Messages older than this are archived automatically. Set to '0' to turn off automatic archiving.")
	int durationForMessageArchive = 0;

	@DcemGui(separator = "FIDO Authentication", help = "Comma-separated list of allowed origins for FIDO Authentications.", style = "width: 650px")
	String fidoAllowedOrigins = null;

	@DcemGui(separator = "CloudSafe & PasswordSafe", help = "MB")
	double cloudSafeDefaultLimit = 50;

	@DcemGui(help = "If checked, PasswordSafe is enabled for users who do not have specified space limit set.")
	boolean passwordSafeEnabledByDefault = true;
	
	@DcemGui()
	boolean enableAuditUser = false;

	public MsgStoragePolicy getMessageStorePolicy() {
		return messageStorePolicy;
	}

	public void setMessageStorePolicy(MsgStoragePolicy messageStorePolicy) {
		this.messageStorePolicy = messageStorePolicy;
	}

	public int getActivationCodeLength() {
		return activationCodeLength;
	}

	public void setActivationCodeLength(int activationCodeLength) {
		this.activationCodeLength = activationCodeLength;
	}

	public boolean isNumericActivationCode() {
		return numericActivationCode;
	}

	public void setNumericActivationCode(boolean numericActivationCode) {
		this.numericActivationCode = numericActivationCode;
	}

	public int getActivationCodeDefaultValidTill() {
		return activationCodeDefaultValidTill;
	}

	public void setActivationCodeDefaultValidTill(int activationCodeDefaultValidTill) {
		this.activationCodeDefaultValidTill = activationCodeDefaultValidTill;
	}

	public int getRetryActivationDelayinMinutes() {
		return retryActivationDelayinMinutes;
	}

	public void setRetryActivationDelayinMinutes(int retryActivationDelayinMinutes) {
		this.retryActivationDelayinMinutes = retryActivationDelayinMinutes;
	}

	public void setMaxFailActivations(int maxFailActivations) {
		this.maxFailActivations = maxFailActivations;
	}

	public int getMaxFailActivations() {
		return maxFailActivations;
	}

//	public int getLoginRertyCounter() {
//		return loginRertyCounter;
//	}
//
//	public void setLoginRertyCounter(int loginRertyCounter) {
//		this.loginRertyCounter = loginRertyCounter;
//	}

	public int getLoginQrCodeResponseTimeout() {
		return loginQrCodeResponseTimeout;
	}

	public void setLoginQrCodeResponseTimeout(int loginQrCodeResponseTimeout) {
		this.loginQrCodeResponseTimeout = loginQrCodeResponseTimeout;
	}

	public int getMessageResponseTimeout() {
		return messageResponseTimeout;
	}

	public void setMessageResponseTimeout(int messageResponseTimeout) {
		this.messageResponseTimeout = messageResponseTimeout;
	}

	public int getMessageRetrieveTimeoutSec() {
		return messageRetrieveTimeoutSec;
	}

	public void setMessageRetrieveTimeoutSec(int messageRetrieveTimeoutSec) {
		this.messageRetrieveTimeoutSec = messageRetrieveTimeoutSec;
	}

	public int getRequestActivationCodeValidTill() {
		return requestActivationCodeValidTill;
	}

	public void setRequestActivationCodeValidTill(int requestActivationCodeValidTill) {
		this.requestActivationCodeValidTill = requestActivationCodeValidTill;
	}

	public int getMaxMessageQueueLength() {
		return maxMessageQueueLength;
	}

	public void setMaxMessageQueueLength(int maxMessageQueueLength) {
		this.maxMessageQueueLength = maxMessageQueueLength;
	}

	public int getPasscodeValidFor() {
		return passcodeValidFor;
	}

	public void setPasscodeValidFor(int passcodeValidFor) {
		this.passcodeValidFor = passcodeValidFor;
	}

	public int getKeepAliveConnection() {
		return keepAliveConnection;
	}

	public void setKeepAliveConnection(int keepAliveConnection) {
		this.keepAliveConnection = keepAliveConnection;
	}

	@XmlElement(name="domainName")  // for compatibility reasons
	public String getRealmName() {
		return realmName;
	}

	

	public boolean isEnableAppAutoVersionRegistration() {
		return enableAppAutoVersionRegistration;
	}

	public void setEnableAppAutoVersionRegistration(boolean enableAppAutoVersionRegistration) {
		this.enableAppAutoVersionRegistration = enableAppAutoVersionRegistration;
	}

	public String getDefaultTemplate() {
		return defaultTemplate;
	}

	public void setDefaultTemplate(String defaultTemplate) {
		this.defaultTemplate = defaultTemplate;
	}

	public int getDurationForMessageArchive() {
		return durationForMessageArchive;
	}

	public void setDurationForMessageArchive(int durationForMessageArchive) {
		this.durationForMessageArchive = durationForMessageArchive;
	}

	public boolean isClearQueuedMsgsOnNewMsg() {
		return clearQueuedMsgsOnNewMsg;
	}

	public void setClearQueuedMsgsOnNewMsg(boolean clearQueuedMsgsOnNewMsg) {
		this.clearQueuedMsgsOnNewMsg = clearQueuedMsgsOnNewMsg;
	}

	public String getFidoAllowedOrigins() {
		return fidoAllowedOrigins;
	}

	public void setFidoAllowedOrigins(String fidoAllowedOrigins) {
		this.fidoAllowedOrigins = fidoAllowedOrigins;
	}

	public double getCloudSafeDefaultLimit() {
		return cloudSafeDefaultLimit;
	}

	public void setCloudSafeDefaultLimit(double cloudSafeDefaultLimit) {
		this.cloudSafeDefaultLimit = cloudSafeDefaultLimit;
	}

	public boolean isPasswordSafeEnabledByDefault() {
		return passwordSafeEnabledByDefault;
	}

	public void setPasswordSafeEnabledByDefault(boolean passwordSafeEnabledByDefault) {
		this.passwordSafeEnabledByDefault = passwordSafeEnabledByDefault;
	}

	public int getPasscodeWindow() {
		return passcodeWindow;
	}

	public void setPasscodeWindow(int passcodeWindow) {
		this.passcodeWindow = passcodeWindow;
	}

	public int getAppReLoginWithin() {
		return appReLoginWithin;
	}

	public void setAppReLoginWithin(int appReLoginWithin) {
		this.appReLoginWithin = appReLoginWithin;
	}

	public void setRealmName(String realmName) {
		this.realmName = realmName;
	}

	public boolean isEnableAuditUser() {
		return enableAuditUser;
	}

	public void setEnableAuditUser(boolean enableAuditUser) {
		this.enableAuditUser = enableAuditUser;
	}
}
