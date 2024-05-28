package com.doubleclue.dcem.core.licence;

import java.security.spec.AlgorithmParameterSpec;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.AdminTenantData;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.tasks.ReloadClassInterface;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.system.send.SendEmail;
import com.doubleclue.utils.StringUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Named("licenceLogic")
public class LicenceLogic implements ReloadClassInterface {

	private static final Logger logger = LogManager.getLogger(LicenceLogic.class);

	@Inject
	ConfigLogic configLogic;

	@Inject
	private DcemApplicationBean dcemApplicationBean;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	TaskExecutor taskExecutor;

	private static final byte[] ENCRYPTION_KEY = { (byte) 0xAB, (byte) 0x32, (byte) 0x76, (byte) 0xFF, (byte) 0x3D, (byte) 0xB4, (byte) 0x77, (byte) 0x29,
			(byte) 0x0E, (byte) 0x82, (byte) 0xD6, (byte) 0xB3, (byte) 0xBB, (byte) 0x08, (byte) 0xC3, (byte) 0xD0 };

	private static final String ENCRYPTION_ALGORITHM = "AES";
	private static final String CIPHER_TYPE = "AES/CBC/PKCS5Padding";
	private final String RESOURCE_PREFIX_WARNING = "licence.warning.";
	private final int LICENCE_WARNING_USER_THRESHOLD_PERCENTAGE = 90;

	private boolean expiredLicenceUserShouldAuthenticate = true;

	// PRIVATE METHODS

	public void checkForLicence(AuthApplication application, boolean allowChanceAfterExpiration) throws DcemException {
		AdminTenantData adminTenantData = adminModule.getTenantData();
		LicenceKeyContent licenceKeyContent = adminTenantData.getLicenceKeyContent();

		if (licenceKeyContent == null) {
			return;
		}

		boolean expiredLicence = licenceKeyContent.getLdtExpiresOn().isBefore(LocalDateTime.now());
		if (expiredLicence) {
			if (allowChanceAfterExpiration && expiredLicenceUserShouldAuthenticate) {
				expiredLicenceUserShouldAuthenticate = !expiredLicenceUserShouldAuthenticate;
				throw new DcemException(DcemErrorCodes.LICENCE_EXPIRED, "Cannot authenticate user - licence expired.");
			}
		}
		
		boolean exceedsUserCount = (userLogic.getTotalUserCount()) >= licenceKeyContent.getMaxUsers();
		if (exceedsUserCount) {
			if (allowChanceAfterExpiration && expiredLicenceUserShouldAuthenticate) {
					expiredLicenceUserShouldAuthenticate = !expiredLicenceUserShouldAuthenticate;
					throw new DcemException(DcemErrorCodes.LICENCE_MAX_USER, "Maximum user licence reached MaxUsers:  " + userLogic.getTotalUserCount());
				}
			}

		if (exceedsUserCount || expiredLicence) {
			expiredLicenceUserShouldAuthenticate = !expiredLicenceUserShouldAuthenticate;
		}
	}

	// PUBLIC METHODS

	public byte[] getEncryptedLicence(LicenceKeyContent licenceContent) throws DcemException {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String json = objectMapper.writeValueAsString(licenceContent);
			byte[] jsonBytes = json.getBytes(DcemConstants.CHARSET_UTF8);

			byte[] clusterIdBytes = licenceContent.getClusterId().getBytes(DcemConstants.CHARSET_UTF8);
			AlgorithmParameterSpec iv = new IvParameterSpec(clusterIdBytes);

			Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(ENCRYPTION_KEY, ENCRYPTION_ALGORITHM), iv);
			byte[] encryptedJson = cipher.doFinal(jsonBytes);
			return encryptedJson;
		} catch (Exception e) {
			logger.error(e);
			throw new DcemException(DcemErrorCodes.INVALID_LICENCE_CONTENT, "Could not encrypt Licence Content: " + licenceContent);
		}
	}

	private LicenceKeyContent getDecryptedLicence(byte[] encryptedLicence) throws DcemException {
		try {
			ClusterConfig clusterConfig = DcemCluster.getDcemCluster().getClusterConfig();
			String clusterId = clusterConfig.getName();
			byte[] clusterIdBytes = clusterId.getBytes(DcemConstants.CHARSET_UTF8);
			AlgorithmParameterSpec iv = new IvParameterSpec(clusterIdBytes);
			Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(ENCRYPTION_KEY, ENCRYPTION_ALGORITHM), iv);
			byte[] jsonBytes = cipher.doFinal(encryptedLicence);
			String json = new String(jsonBytes, DcemConstants.CHARSET_UTF8);
			LicenceKeyContent licenceKeyContent;
			ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			licenceKeyContent = objectMapper.readValue(json, LicenceKeyContent.class);
			return licenceKeyContent;
		} catch (Exception e) {
			logger.error("Could not decrypt Licence Content ", e);
			throw new DcemException(DcemErrorCodes.INVALID_LICENCE_CONTENT, "Could not decrypt Licence Content. Please check your Cluster ID.");
		}
	}

	public LicenceKeyContent getDecryptedLicence(String licenceKey) throws DcemException {
		try {
			byte[] encryptedLicenceBytes = java.util.Base64.getDecoder().decode(licenceKey);
			return getDecryptedLicence(encryptedLicenceBytes);
		} catch (DcemException e) {
			logger.error(e);
			throw e;
		}
	}

	public LicenceKeyContent loadLicenceKeyContent() throws DcemException {
		try {
			DcemConfiguration dcemConfiguration = configLogic.getDcemConfiguration(AdminModule.MODULE_ID, DcemConstants.CONFIG_KEY_LICENCE);
			AdminTenantData adminTenantData = adminModule.getAdminTenantData();
			if (dcemConfiguration == null) {
				LicenceKeyContent licenceKeyContent = createTrialLicence(DcemConstants.LICENCE_TRIAL_EXPIRY_DAYS, "Trial Licence");
				adminTenantData.setLicenceKeyContent(licenceKeyContent);
				return licenceKeyContent;
			}
			byte[] encryptedLicence = dcemConfiguration.getValue();
			LicenceKeyContent licenceKeyContent = getDecryptedLicence(encryptedLicence);
			adminTenantData.setLicenceKeyContent(licenceKeyContent);
			if (licenceKeyContent.getDisabledModules() != null) {
				String[] disabledModules = licenceKeyContent.getDisabledModules().split(",");
				for (int i = 0; i < disabledModules.length; i++) {
					disabledModules[i] = disabledModules[i].trim();
				}
				adminTenantData.setDisabledModules(disabledModules);
			} else {
				adminTenantData.setDisabledModules(null);
			}
			if (licenceKeyContent.getPluginModules() != null) {
				String[] pluginModulesModules = licenceKeyContent.getPluginModules().split(",");
				for (int i = 0; i < pluginModulesModules.length; i++) {
					pluginModulesModules[i] = pluginModulesModules[i].trim();
				}
				adminTenantData.setEnabledPluginModules(pluginModulesModules);
			} else {
				adminTenantData.setEnabledPluginModules(null);
			}
			return licenceKeyContent;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.INVALID_LICENCE_CONTENT, "Could not read LicenceContent");
		}
	}

	public void resetExpiredLicenceUserShouldAuthenticate() {
		expiredLicenceUserShouldAuthenticate = true;
	}

	public LicenceKeyContent createTrialLicence(int days, String customerName) throws DcemException {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_YEAR, days);
		Date expiryDate = c.getTime();
		String clusterId = DcemCluster.getDcemCluster().getClusterConfig().getName();
		return new LicenceKeyContent(clusterId, customerName, expiryDate, TenantIdResolver.getCurrentTenantName(), DcemConstants.LICENCE_KEY_VERSION,
				DcemConstants.LICENCE_PLUGINS_ALL);
	}

	public String getEncryptedLicenceAsString(LicenceKeyContent licenceKeyContent) throws DcemException {
		try {
			byte[] encryptedLicence = getEncryptedLicence(licenceKeyContent);
			return java.util.Base64.getEncoder().encodeToString(encryptedLicence);
		} catch (DcemException e) {
			throw e;
		}
	}

	public void addLicenceToDb(LicenceKeyContent licenceContent) throws DcemException {
		String tenantId = licenceContent.tenantId;
		if (((tenantId == null || tenantId.isEmpty()) && TenantIdResolver.isCurrentTenantMaster())
				|| (tenantId != null && tenantId.equals(TenantIdResolver.getCurrentTenantName()))) {
			try {
				byte[] encryptedLicence = getEncryptedLicence(licenceContent);
				// check if there is already a licence saved for this module
				DcemConfiguration semConfiguration = configLogic.getDcemConfiguration(AdminModule.MODULE_ID, DcemConstants.CONFIG_KEY_LICENCE);
				if (semConfiguration == null) {
					semConfiguration = new DcemConfiguration(AdminModule.MODULE_ID, DcemConstants.CONFIG_KEY_LICENCE, encryptedLicence);
				} else {
					semConfiguration.setValue(encryptedLicence);
				}
				configLogic.setDcemConfiguration(semConfiguration);
			} catch (Exception exp) {
				logger.error("Couldn't create licence", exp);
				throw new DcemException(DcemErrorCodes.INVALID_LICENCE_CONTENT, "Could not set licence: " + licenceContent);
			}
		} else {
			throw new DcemException(DcemErrorCodes.INVALID_LICENCE_CONTENT,
					"Wrong tenant while applying licence for: " + tenantId + " Current Tenant: " + TenantIdResolver.getCurrentTenantName());
		}
	}

	public void setLicence(LicenceKeyContent licenceKeyContent, TenantEntity tenantEntity) throws Exception {
		Future<Exception> future = taskExecutor.submit(new Callable<Exception>() {
			@Override
			public Exception call() throws Exception {
				TenantIdResolver.setCurrentTenant(tenantEntity);
				WeldRequestContext requestContext = WeldContextUtils.activateRequestContext();
				try {
					addLicenceToDb(licenceKeyContent);
					loadLicenceKeyContent();
					return null;
				} catch (Exception e) {
					return e;
				} finally {
					WeldContextUtils.deactivateRequestContext(requestContext);
				}
			}
		});
		Exception exception = future.get();
		if (exception != null) {
			throw exception;
		}
	}

	public void checkLicenceAlerts() {
		Map<String, List<DcemReporting>> moduleWarnings = getLicenceAlertsFromModules();
		for (Entry<String, List<DcemReporting>> entry : moduleWarnings.entrySet()) {
			for (DcemReporting alert : entry.getValue()) {
				try {
					if (reportingLogic.welcomeViewAlertExists(alert) == false) {
						reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, alert);
					}
				} catch (Exception e) {
					logger.debug(e);
				}
			}
		}
	}

	public List<DcemReporting> getLicenceWarnings() {
		List<DcemReporting> alerts = new ArrayList<DcemReporting>();
		AdminTenantData adminTenantData = adminModule.getTenantData();
		LicenceKeyContent licenceKeyContent = adminTenantData.getLicenceKeyContent();
		LocalDateTime expiryDate = licenceKeyContent.getLdtExpiresOn();
		LocalDateTime now = LocalDateTime.now();
		int userCount = userLogic.getTotalUserCount();
		int maxUsers = licenceKeyContent.getMaxUsers();

		if (licenceKeyContent.isTrialVersion()) {
			alerts.add(createAlertMessage(getLocalisedWarning("trialLicence"), AlertSeverity.WARNING));
		}

		if (userCount >= maxUsers) {
			alerts.add(createAlertMessage(getLocalisedWarning("maxUsersExceeded", maxUsers), AlertSeverity.WARNING));
		} else {
			int remainingUsers = maxUsers - userCount;
			int threshold = (int) (((100f - LICENCE_WARNING_USER_THRESHOLD_PERCENTAGE) / 100f) * maxUsers);
			if (remainingUsers < threshold) {
				alerts.add(createAlertMessage(getLocalisedWarning("maxUsersAlmostReached", remainingUsers), AlertSeverity.WARNING));
			}
		}

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, operatorSessionBean.getLocale());
		if (expiryDate.isBefore(now)) {
			LocalDateTime localDateTimeExpire = expiryDate.plusDays(DcemConstants.LICENCE_ACTIVATION_EXPIRY_GRACE_PERIOD_DAYS);

			if (expiryDate.isBefore(localDateTimeExpire)) {
				alerts.add(createAlertMessage(getLocalisedWarning("licenceExpiredGracePeriod", df.format(expiryDate)), AlertSeverity.ERROR));
			} else {
				alerts.add(createAlertMessage(getLocalisedWarning("licenceExpired", df.format(expiryDate)), AlertSeverity.ERROR));
			}
		} else {
			LocalDateTime localDateTimeWarinig = expiryDate.minusDays(DcemConstants.LICENCE_ACTIVATION_EXPIRY_GRACE_PERIOD_DAYS);
			if (localDateTimeWarinig.isBefore(now)) {
				alerts.add(createAlertMessage(getLocalisedWarning("licenceAlmostExpired", df.format(expiryDate)), AlertSeverity.WARNING));
			}
		}
		return alerts;
	}

	private String getLocalisedWarning(String key, Object... parameters) {
		String message = JsfUtils.getStringSafely(AdminModule.RESOURCE_NAME, RESOURCE_PREFIX_WARNING + key);
		return parameters.length > 0 ? new MessageFormat(message).format(parameters) : message;
	}

	public Map<String, List<DcemReporting>> getLicenceAlertsFromModules() {
		Map<String, List<DcemReporting>> licenceWarnings = new HashMap<>();
		for (DcemModule module : dcemApplicationBean.getSortedModules()) {
			List<DcemReporting> alerts = module.getLicenceAlerts();
			if (alerts != null && !alerts.isEmpty()) {
				licenceWarnings.put(module.getName(), alerts);
			}
		}
		return licenceWarnings;
	}

	public void sendLicenceWarningEmails() throws DcemException {
		Map<String, List<DcemReporting>> moduleAlerts = getLicenceAlertsFromModules();
		if (moduleAlerts.isEmpty() == false) {
			List<DcemUser> admins = userLogic.getAdminOperators();
			if (admins != null && !admins.isEmpty()) {
				String moduleWarningsString = "";
				for (Entry<String, List<DcemReporting>> entry : moduleAlerts.entrySet()) {
					moduleWarningsString += entry.getKey() + ":<br>";
					for (DcemReporting warning : entry.getValue()) {
						moduleWarningsString += "&nbsp•&nbsp" + warning.getInfo() + "<br>";
					}
				}
				for (DcemUser admin : admins) {
					String email = admin.getEmail();
					if (email != null && !email.isEmpty()) {
						DbResourceBundle bundle = DbResourceBundle.getDbResourceBundle(admin.getLanguage().getLocale());
						DcemTemplate bodyTemplate = templateLogic.getTemplateByNameLanguage(DcemConstants.EMAIL_LICENCE_WARNING_BODY_TEMPLATE,
								admin.getLanguage());
						if (bodyTemplate == null) {
							throw new DcemException(DcemErrorCodes.NO_TEMPLATE_FOUND, "Missing template: " + DcemConstants.EMAIL_LICENCE_WARNING_BODY_TEMPLATE);
						}
						Map<String, String> map = new HashMap<>();
						map.put(DcemConstants.EMAIL_LICENCE_WARNING_KEY, moduleWarningsString);
						map.put(DcemConstants.EMAIL_LICENCE_USER_KEY, admin.getDisplayNameOrLoginId());
						String body = StringUtils.substituteTemplate(bodyTemplate.getContent(), map);
						SendEmail.sendMessage(email, body, bundle.getString(DcemConstants.EMAIL_LICENCE_WARNING_SUBJECT_BUNDLE_KEY));
					}
				}
			}
		}
	}

	private int getDaysBetweenDates(Date date1, Date date2) {
		return (int) Math.abs(((date1.getTime() - date2.getTime()) / 86400000)); // milliseconds in a day
	}

	private DcemReporting createAlertMessage(String message, AlertSeverity severity) {
		DcemReporting alertMessage = new DcemReporting();
		alertMessage.setErrorCode(DcemErrorCodes.INVALID_LICENCE_CONTENT.toString());
		alertMessage.setAction(ReportAction.Licence);
		alertMessage.setSeverity(severity);
		alertMessage.setInfo(message);
		alertMessage.setShowOnDashboard(true);
		alertMessage.setLocalDateTime(LocalDateTime.now());
		alertMessage.setSource("Licence Administration");
		return alertMessage;
	}

	public LicenceKeyContent getLicenceKeyContent() throws DcemException {
		AdminTenantData adminTenantData = adminModule.getTenantData();
		LicenceKeyContent licenceKeyContent = adminTenantData.getLicenceKeyContent();
		if (licenceKeyContent == null) {
			throw new DcemException(DcemErrorCodes.LICENCE_NOT_AVAILABLE, "");
		}
		return licenceKeyContent;
	}

	public LicenceKeyContentUsage getTenantLicenceKeyUsage(TenantEntity tenantEntity) throws Exception {
		Future<LicenceKeyContentUsage> future = taskExecutor.submit(new Callable<LicenceKeyContentUsage>() {
			@Override
			public LicenceKeyContentUsage call() throws Exception {
				TenantIdResolver.setCurrentTenant(tenantEntity);
				WeldRequestContext requestContext = WeldContextUtils.activateRequestContext();
				try {
					return getLicenceKeyContentUsage();
				} finally {
					WeldContextUtils.deactivateRequestContext(requestContext);
				}
			}

		});
		return future.get();
	}

	public LicenceKeyContentUsage getLicenceKeyContentUsage() throws DcemException {
		AsModuleApi asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
		return new LicenceKeyContentUsage(userLogic.getTotalUserCount(), asModuleApi.getCloudSafeUsageMb(), getLicenceKeyContent());
	}

	@Override
	public void reload(String info) throws DcemException {
		loadLicenceKeyContent();
	}

}
