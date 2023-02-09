package com.doubleclue.dcem.system.logic;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logging.DcemLogLevel;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@XmlType
@XmlRootElement(name = "systemPreferences")
public class SystemPreferences extends ModulePreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@DcemGui(separator = "Log Configuration", help = "Please do not enter any data here if not instructed to do so by the DoubleClue support team.")
	String additionalLoggers = "";

	@DcemGui
	boolean enableMonitoring;

	@DcemGui(help = "in Minutes.")
	@Min(0)
	int monitoringInterval = 10;

	@DcemGui
	boolean enableDbStatistics;

	public boolean isEnableDbStatistics() {
		return enableDbStatistics;
	}

	public void setEnableDbStatistics(boolean enableDbStatistics) {
		this.enableDbStatistics = enableDbStatistics;
	}

	public boolean isEnableMonitoring() {
		return enableMonitoring;
	}

	public void setEnableMonitoring(boolean enableMonitoring) {
		this.enableMonitoring = enableMonitoring;
	}

	public int getMonitoringInterval() {
		return monitoringInterval;
	}

	public void setMonitoringInterval(int monitoringInterval) {
		this.monitoringInterval = monitoringInterval;
	}

	@DcemGui(help = "The amount of record entries that will be stored. Once this number is exceeded, the oldest entries will be overwritten.")
	int maxMonitoringRecords = 1000;

	@DcemGui
	DcemLogLevel logLevel = DcemLogLevel.INFO;

	@DcemGui(separator = "HTTP Proxy Configuration", help = "'0' means do not use HTTP Proxy.")
	@Max(65535)
	int httpProxyPort = 0;

	@DcemGui(style = "width: 300px")
	@Size(max = 128)
	String httpProxyHost;

	@DcemGui
	@Size(max = 128)
	String httpProxyUser;

	@DcemGui(password = true)
	@Size(max = 128)
	String httpProxyPassword;

	@DcemGui(separator = "E-Mail SSL/TLS Configuration", help = "Leave empty if e-mail configuration is not required.", style = "width: 300px")
	String eMailHostAddress;

	@DcemGui(help = "'0' means no e-mail configuration is required.")
	@Max(65535)
	int eMailHostPort = 0;

	@DcemGui(style = "width: 300px")
	String eMailUser;

	@DcemGui(style = "width: 300px", password = true)
	String eMailPassword;

	@DcemGui(help = "This is the source e-mail shown.", style = "width: 300px")
	String eMailFromEmail;

	@DcemGui(help = "This is the source e-mail person.", style = "width: 300px")
	String eMailFromPerson;

	@DcemGui(help = "Protocl used for E-Mail sent.")
	String eMailProtocol = "TLSv1.2";

	@DcemGui(separator = "SMS Configuration - Provider www.messagebird.com", password = true, help = "This field should be empty if no SMS is being used.", style = "width: 300px")
	String smsProviderAccesKey;

	@DcemGui()
	String smsOriginatorName;

	@DcemGui(separator = "Google CAPTCHA", style = "width: 500px", password = true)
	String captchaPrivateKey;

	@DcemGui(style = "width: 500px", password = true)
	String captchaPublicKey;
	
	@DcemGui(separator = "HTTP Protection. Changes will require a restart.")
	boolean strictTransportSecurityEnabled = true;
	
	@DcemGui()
	boolean antiClickJackingEnabled = true;
	
	@DcemGui()
	boolean blockContentTypeSniffingEnabled = true;
	
	@DcemGui()
	boolean xssProtectionEnabled = true;	

	@DcemGui(separator = "Embedded Database", help = "If you use the Embedded Database, this will do a backup of the Embedded Database on every 'Nightly task'. During the backup process,"
			+ " writing into the database will be blocked!")
	boolean runEmbeddedDatabaseBackup;

	@DcemGui(style = "width: 400px", help = "This is the absolute directory path for the Embedded Database backup.")
	String pathEmbeddedDatabaseBackup;

	@DcemGui(separator = "Other", help = "Nightly tasks refers to scheduled automatic maintenance services performed on a daily base. Normally, these tasks will run at night but you can choose any hour of day by defining it in this field.",
			// choose = "#{systemPreferences.timeOptions}")
			choose = { "00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00",
					"15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00" })
	String nightlyTaskTime = "02:00";

	@DcemGui(style = "width: 500px", help = "Please do not enter any data here if not instructed to do so by the DoubleClue support team.")
	String specialProperties;
	
	@DcemGui (help = "If on, all outgoing API calls will be traced in the debug logger.")
	boolean traceRestApi;

	public String getAdditionalLoggers() {
		return additionalLoggers;
	}

	public void setAdditionalLoggers(String additionalLoggers) {
		this.additionalLoggers = additionalLoggers;
	}

	public DcemLogLevel getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(DcemLogLevel logLevel) {
		this.logLevel = logLevel;
	}

	public int getHttpProxyPort() {
		return httpProxyPort;
	}

	public void setHttpProxyPort(int httpProxyPort) {
		this.httpProxyPort = httpProxyPort;
	}

	public String getHttpProxyHost() {
		return httpProxyHost;
	}

	public void setHttpProxyHost(String httpProxyHost) {
		this.httpProxyHost = httpProxyHost;
	}

	public String getHttpProxyUser() {
		return httpProxyUser;
	}

	public void setHttpProxyUser(String httpProxyUser) {
		this.httpProxyUser = httpProxyUser;
	}

	public String getHttpProxyPassword() {
		return httpProxyPassword;
	}

	public void setHttpProxyPassword(String httpProxyPassword) {
		this.httpProxyPassword = httpProxyPassword;
	}

	public String geteMailHostAddress() {
		return eMailHostAddress;
	}

	public void seteMailHostAddress(String eMailHostAddress) {
		this.eMailHostAddress = eMailHostAddress;
	}

	public int geteMailHostPort() {
		return eMailHostPort;
	}

	public void seteMailHostPort(int eMailHostPort) {
		this.eMailHostPort = eMailHostPort;
	}

	public String geteMailUser() {
		return eMailUser;
	}

	public void seteMailUser(String eMailUser) {
		this.eMailUser = eMailUser;
	}

	public String geteMailPassword() {
		return eMailPassword;
	}

	public void seteMailPassword(String eMailPassword) {
		this.eMailPassword = eMailPassword;
	}

	public String geteMailFromEmail() {
		return eMailFromEmail;
	}

	public void seteMailFromEmail(String eMailFromEmail) {
		this.eMailFromEmail = eMailFromEmail;
	}

	public String geteMailFromPerson() {
		return eMailFromPerson;
	}

	public void seteMailFromPerson(String eMailFromPerson) {
		this.eMailFromPerson = eMailFromPerson;
	}

	public String getSmsProviderAccesKey() {
		return smsProviderAccesKey;
	}

	public void setSmsProviderAccesKey(String smsProviderAccesKey) {
		this.smsProviderAccesKey = smsProviderAccesKey;
	}

	public String getSmsOriginatorName() {
		return smsOriginatorName;
	}

	public void setSmsOriginatorName(String smsOriginatorName) {
		this.smsOriginatorName = smsOriginatorName;
	}

	public String getNightlyTaskTime() {
		return nightlyTaskTime;
	}

	public void setNightlyTaskTime(String nightlyTaskTime) {
		this.nightlyTaskTime = nightlyTaskTime;
	}

	public String[] getTimeOptions() {
		String[] timeOptions = new String[24];
		for (int i = 0; i < 24; i++) {
			timeOptions[i] = String.format("%02d", i) + ":00";
		}
		return timeOptions;
	}

	public int getMaxMonitoringRecords() {
		return maxMonitoringRecords;
	}

	public void setMaxMonitoringRecords(int maxMonitoringRecords) {
		this.maxMonitoringRecords = maxMonitoringRecords;
	}

	public String getPathEmbeddedDatabaseBackup() {
		return pathEmbeddedDatabaseBackup;
	}

	public void setPathEmbddedDatabaseBackup(String pathEmbddedDatabaseBackup) {
		this.pathEmbeddedDatabaseBackup = pathEmbddedDatabaseBackup;
	}

	public String getSpecialProperties() {
		return specialProperties;
	}

	public void setSpecialProperties(String specialProperties) {
		this.specialProperties = specialProperties;
	}

	public boolean isRunEmbeddedDatabaseBackup() {
		return runEmbeddedDatabaseBackup;
	}

	public void setRunEmbeddedDatabaseBackup(boolean runEmbeddedDatabaseBackup) {
		this.runEmbeddedDatabaseBackup = runEmbeddedDatabaseBackup;
	}

	public void setPathEmbeddedDatabaseBackup(String pathEmbeddedDatabaseBackup) {
		this.pathEmbeddedDatabaseBackup = pathEmbeddedDatabaseBackup;
	}

	public String getCaptchaPrivateKey() {
		return captchaPrivateKey;
	}

	public void setCaptchaPrivateKey(String captchaPrivateKey) {
		this.captchaPrivateKey = captchaPrivateKey;
	}

	public String getCaptchaPublicKey() {
		return captchaPublicKey;
	}

	public void setCaptchaPublicKey(String captchaPublicKey) {
		this.captchaPublicKey = captchaPublicKey;
	}

	public String geteMailProtocol() {
		return eMailProtocol;
	}

	public void seteMailProtocol(String eMailProtocol) {
		this.eMailProtocol = eMailProtocol;
	}

	public boolean isStrictTransportSecurityEnabled() {
		return strictTransportSecurityEnabled;
	}

	public void setStrictTransportSecurityEnabled(boolean strictTransportSecurityEnabled) {
		this.strictTransportSecurityEnabled = strictTransportSecurityEnabled;
	}

	public boolean isAntiClickJackingEnabled() {
		return antiClickJackingEnabled;
	}

	public void setAntiClickJackingEnabled(boolean antiClickJackingEnabled) {
		this.antiClickJackingEnabled = antiClickJackingEnabled;
	}

	public boolean isBlockContentTypeSniffingEnabled() {
		return blockContentTypeSniffingEnabled;
	}

	public void setBlockContentTypeSniffingEnabled(boolean blockContentTypeSniffingEnabled) {
		this.blockContentTypeSniffingEnabled = blockContentTypeSniffingEnabled;
	}

	public boolean isXssProtectionEnabled() {
		return xssProtectionEnabled;
	}

	public void setXssProtectionEnabled(boolean xssProtectionEnabled) {
		this.xssProtectionEnabled = xssProtectionEnabled;
	}

	public boolean isTraceRestApi() {
		return traceRestApi;
	}

	public void setTraceRestApi(boolean traceRestApi) {
		this.traceRestApi = traceRestApi;
	}

}
