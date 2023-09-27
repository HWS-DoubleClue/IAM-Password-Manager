package com.doubleclue.dcem.as.logic;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.comm.AppServices;
import com.doubleclue.dcem.as.comm.AppWsConnection;
import com.doubleclue.dcem.as.comm.EndPointConfiguration;
import com.doubleclue.dcem.as.comm.PendingMessageListener;
import com.doubleclue.dcem.as.comm.client.ConnectDcemProxyTask;
import com.doubleclue.dcem.as.comm.client.ProxyCommClient;
import com.doubleclue.dcem.as.comm.client.ReverseProxyState;
import com.doubleclue.dcem.as.comm.client.RpClientAction;
import com.doubleclue.dcem.as.comm.client.RpReport;
import com.doubleclue.dcem.as.entities.AuthGatewayEntity;
import com.doubleclue.dcem.as.entities.FingerprintId;
import com.doubleclue.dcem.as.entities.MessageEntity;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.policy.FingerprintLogic;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionService;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.ExportRecords;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.SecureUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.IMap;
import com.hazelcast.flakeidgen.FlakeIdGenerator;

@SuppressWarnings("serial")
@ApplicationScoped
@Named("asModule")
public class AsModule extends DcemModule {

	private static Logger logger = LogManager.getLogger(AsModule.class);

	public final static String MODULE_ID = DcemConstants.AS_MODULE_ID;

	@Inject
	ConfigLogic configLogic;

	@Inject
	AppServices appServices;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	TemplateLogic asTemplateLogic;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	SystemModule systemModule;

	@Inject
	LicenceLogic licenceLogic;

	@Inject
	AsActivationLogic activationLogic;

	@Inject
	CloudSafeLogic cloudDataLogic;

	@Inject
	AsVersionLogic versionLogic;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	ProxyCommClient proxyCommClient;

	@Inject
	RoleLogic roleLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	PolicyLogic policyLogic;

	@Inject
	AsMessageLogic messageLogic;

	@Inject
	AsAuthGatewayLogic authGatewayLoigic;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	ExportRecords exportRecords;

	@Inject
	FingerprintLogic fingerprintLogic;

	@Inject
	UrlTokenLogic urlTokenLogic;

	@Inject
	FcmLogic fcmLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	DomainLogic domainLogic;

	@Inject
	AsFidoLogic asFidoLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	ScheduledFuture<?> reverseProxySchedule;

	public final static String RESOURCE_NAME = "com.doubleclue.dcem.as.resources.Messages";
	public final static String DEFAULT_ARCHIVE_PATH = "\\AsReportArchives\\";

	// we should not change this. We will try to put all migration files into the
	// system.
	final static int AS_DB_VERSION = 3;

	private PrivateKey privateKey;
	private PublicKey publicKey;
	private String connectionKey;
	private byte[] connectionKeyArray;

	boolean enableAppAutoVersionRegistration;
	String realmName;

	private PublicKey dispatcherPublicKey;

	public AsModule() {
		super();
	}

	@Override
	public void init() throws DcemException {

	}

	public int getRank() {
		return 30;
	}

	public String getName() {
		return "Identity & Access";
	}

	@Override
	public void initializeDb(DcemUser superAdmin) throws DcemException {
		if (TenantIdResolver.isCurrentTenantMaster()) {
			String googleServiceFile = null;
			try {
				byte[] data = KaraUtils.readInputStream(this.getClass().getResourceAsStream(AsConstants.FCM_FILE));
				data = SecureServerUtils.decryptDataCommon(data);
				googleServiceFile = new String(data, DcemConstants.UTF_8);
			} catch (Exception e) {
				logger.warn("Couldn't create fcm file", e);
			}
			PushNotificationConfig pushNotificationConfig = new PushNotificationConfig(true, true, googleServiceFile);
			fcmLogic.writeConfiguration(pushNotificationConfig);
		}
	}

	public void start() throws DcemException {
		logger.debug("Starting AS " + TenantIdResolver.getCurrentTenantName());

		super.start();
		ServerEndpointConfig endpointConfig = ServerEndpointConfig.Builder.create(AppWsConnection.class, "/ws/AppConnection")
				.configurator(new EndPointConfiguration()).build();

		ServerContainer serverContainer = (ServerContainer) applicationBean.getServletContext().getAttribute("javax.websocket.server.ServerContainer");

		try {
			serverContainer.addEndpoint(endpointConfig);
		} catch (DeploymentException e) {
			logger.fatal(e);
		}

		try {
			DcemConfiguration dcemConfiguration = configLogic.getDcemConfiguration(AsModule.MODULE_ID, AsConstants.CA_PRIVATE_KEY);
			KeyPair keyPair = null;
			if (dcemConfiguration == null) { // first time that DCEM is run
				try {
					keyPair = SecureUtils.generateKeyPair(DcemConstants.DEFAULT_KEY_PAIR_SIZE);
					dcemConfiguration = new DcemConfiguration(AsModule.MODULE_ID, AsConstants.CA_PRIVATE_KEY, keyPair.getPrivate().getEncoded());
					configLogic.setDcemConfiguration(dcemConfiguration);
					X509Certificate certificate = SecureServerUtils.createCertificate(keyPair.getPublic(), keyPair.getPrivate(), AsConstants.DCEM_AS_CA_ISSUER,
							AsConstants.DCEM_AS_CA_ISSUER, null, null, null);

					dcemConfiguration = new DcemConfiguration(AsModule.MODULE_ID, AsConstants.CA_PUBLIC_KEY, keyPair.getPublic().getEncoded());
					configLogic.setDcemConfiguration(dcemConfiguration);

					dcemConfiguration = new DcemConfiguration(AsModule.MODULE_ID, AsConstants.CA_PUBLIC_CERTIFICATE, certificate.getEncoded());
					configLogic.setDcemConfiguration(dcemConfiguration);

					this.publicKey = keyPair.getPublic();
					this.privateKey = keyPair.getPrivate();
					// String googleServiceFile = null;
					// try {
					// byte[] data =
					// KaraUtils.readInputStream(this.getClass().getResourceAsStream(AsConstants.FCM_FILE));
					// data = SecureServerUtils.decryptDataCommon(data);
					// googleServiceFile = new String(data, DcemConstants.UTF_8);
					// } catch (Exception e) {
					// logger.warn("Couldn't create fcm file", e);
					// }
					// PushNotificationConfig pushNotificationConfig = new
					// PushNotificationConfig(true, true, googleServiceFile);
					// fcmLogic.writeConfiguration(pushNotificationConfig);
				} catch (Exception e) {
					logger.error("Couldn't create CA", e);
				}
			} else {
				try {
					this.privateKey = SecureUtils.loadPrivateKey(dcemConfiguration.getValue());
					dcemConfiguration = configLogic.getDcemConfiguration(AsModule.MODULE_ID, AsConstants.CA_PUBLIC_KEY);
					this.publicKey = SecureUtils.loadPublicKey(dcemConfiguration.getValue());
				} catch (Exception e) {
					logger.error("Couldn't load Key Pair", e);
				}
			}
			connectionKey = DcemCluster.getDcemCluster().getClusterConfig().getName();
			try {
				connectionKeyArray = connectionKey.getBytes(DcemConstants.CHARSET_UTF8);
			} catch (UnsupportedEncodingException exp) {
				logger.warn(exp);
			}

			try {
				byte[] data = KaraUtils.readInputStream(this.getClass().getResourceAsStream(AsConstants.DISPATCHER_PUBLIC_KEY));
				this.dispatcherPublicKey = SecureUtils.loadPublicKey(data);
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.DISPATCHER_KEY_MISSING, e.toString(), e);
			}
			enableAppAutoVersionRegistration = getModulePreferences().isEnableAppAutoVersionRegistration();
			realmName = getModulePreferences().getRealmName();
		} catch (DcemException exp) {
			logger.warn(exp);
		}
		AppWsConnection.getInstance().init(connectionKey, connectionKeyArray);
		appServices.init();
		updateReverseProxy(true, null);
		cloudDataLogic.synchroniseGlobalCloudSafeUsageTotal();
	}

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	@Override
	public DcemView getDefaultView() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 */
	public ModulePreferences getDefaultPreferences() {
		return new AsPreferences();
	}

	public AsPreferences getPreferences() {
		return getModulePreferences();
	}

	@Override
	public AsPreferences getModulePreferences() {
		AsPreferences preferences = (AsPreferences) super.getModulePreferences();
		if (preferences != null) {
			String fidoOrigins = preferences.getFidoAllowedOrigins();
			if ((fidoOrigins == null || fidoOrigins.isEmpty())) {
				URL url = null;
				try {
					url = new URL(JsfUtils.getHttpServletRequest().getRequestURL().toString());
				} catch (Exception exp) {
				}
				if (url != null) {
					try {
						StringBuilder sb = new StringBuilder();
						// TODO This dows not work for sub-Tenants?
						if (TenantIdResolver.isCurrentTenantMaster()) {
							ConnectionService service = configLogic.getClusterConfig().getConnectionService(ConnectionServicesType.MANAGEMENT);
							int mgtPort = -1;
							if (service != null) {
								mgtPort = service.getPort();
								sb.append((service.isSecure() ? "https" : "http") + "://" + url.getHost() + ":" + mgtPort);
							} else {
								sb.append(url.getProtocol() + "://" + url.getHost());
							}
							service = configLogic.getClusterConfig().getConnectionService(ConnectionServicesType.USER_PORTAL);
							if (service != null && service.getPort() != mgtPort) {
								sb.append(",");
								sb.append((service.isSecure() ? "https" : "http") + "://" + url.getHost() + ":" + service.getPort());
							}
							preferences.setFidoAllowedOrigins(sb.toString());
						}
					} catch (Exception e) {
						logger.info("Cannot create default Fido Origins", e);
					}
				}
			}
		}
		return preferences;
	}

	public void stop() throws DcemException {
		started = false;

	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public String getConnectionKey() {
		return connectionKey;
	}

	public void setConnectionKey(String connectionKey) {
		this.connectionKey = connectionKey;
	}

	@Override
	public void preferencesValidation(ModulePreferences modulePreferences) throws DcemException {
		AsPreferences preferences = (AsPreferences) modulePreferences;
		String fidoAllowedOrigins= "";
		if (preferences.getFidoAllowedOrigins() != null) {
			fidoAllowedOrigins = preferences.getFidoAllowedOrigins().replaceAll("\\s+", "");
		}
		if (fidoAllowedOrigins != null && !fidoAllowedOrigins.isEmpty()) {
			if (matchesRegex(fidoAllowedOrigins, "(.+)?[^\\/:][\\/?&](.+)?")) {
				throw new DcemException(DcemErrorCodes.INVALID_FIDO_ALLOWED_ORIGINS, "Invalid FIDO Allowed Origins. Please do not include any paths.");
			} else if (!matchesRegex(fidoAllowedOrigins, "((^|,)https?:\\/\\/[^,]+)+")) {
				throw new DcemException(DcemErrorCodes.INVALID_FIDO_ALLOWED_ORIGINS,
						"Invalid FIDO Allowed Origins. Please make sure they start with http or https.");
			}
		} else {
			throw new DcemException(DcemErrorCodes.INVALID_FIDO_ALLOWED_ORIGINS,
					"A FIDO Server ID was specified, but no allowed origins are present. FIDO authentications cannot be authorised.");
		}

		long cloudSafeLimit = licenceLogic.getLicenceKeyContent().getCloudSafeStoageMb();
		if (preferences.getCloudSafeDefaultLimit() > cloudSafeLimit) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_LIMIT_EXCEEDS_GLOBAL,
					"Default CloudSafe limit exceeds the licence's limit (" + cloudSafeLimit + " MB)");
		}
	}

	private static boolean matchesRegex(String text, String pattern) {
		try {
			return Pattern.compile(pattern).matcher(text).matches();
		} catch (RuntimeException e) {
			return false;
		}
	}

	public void savePreferences(ModulePreferences modulePreferencesPrevious, ModulePreferences modulePreferencesNew) {
		super.savePreferences(modulePreferencesPrevious, modulePreferencesNew);
		if (TenantIdResolver.isCurrentTenantMaster()) {
			enableAppAutoVersionRegistration = getModulePreferences().isEnableAppAutoVersionRegistration();
			realmName = getModulePreferences().getRealmName();
		}

	}

	public byte[] getConnectionKeyArray() {
		return connectionKeyArray;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.doubleclue.dcem.core.logic.module.DcemModule#getStatisticValues()
	 */
	public Map<String, String> getStatisticValues() {
		Map<String, String> map = super.getStatisticValues();

		map.put("Connections", Integer.toString(getTenantData().getDeviceSessions().size()));
		map.put("PendingMsgs", Integer.toString(appServices.getPendingMsgsCount()));
		map.put("PendingLoginQrCodes", Integer.toString(appServices.getloginQrCodesCount()));
		map.put("Connections-ReverseProxy", Integer.toString(proxyCommClient.getConnections()));
		return map;
	}

	public PublicKey getDispatcherPublicKey() {
		return dispatcherPublicKey;
	}

	@Override
	public void runNightlyTask() {
		// Clean expired Activation-Codes
		activationLogic.deleteExpiredActivationCodes();
		// Clean expired Cloud-Data
		cloudDataLogic.deleteExpiredCloudSafe();
		cloudDataLogic.synchroniseGlobalCloudSafeUsageTotal();
		fingerprintLogic.deleteExpiredFingerprints();
		// Archive Reports
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		if (cal.get(Calendar.DAY_OF_MONTH) == 1 || systemModule.getSpecialPropery(DcemConstants.SPECIAL_PROPERTY_RUN_NIGHTLY_TASK) != null) {
			int days = getPreferences().getDurationForMessageArchive();
			if (days > 0) {
				try {
					String[] result = exportRecords.archive(days, MessageEntity.class, MessageEntity.GET_AFTER, MessageEntity.DELETE_AFTER);
					if (result != null) {
						logger.info("SecureMessage-Archived: File=" + result[0] + " Records=" + result[1]);
					}
				} catch (DcemException exp) {
					logger.warn("Couldn't archive SecureMessage", exp);
				}
			}
		}
	}

	/**
	 * @param seconds
	 */
	public void updateReverseProxy(boolean startImmediate, RpConfig rpConfig) {

		DcemConfiguration dcemConfiguration = null;
		proxyCommClient.setState(ReverseProxyState.No_Configuration);
		int startSeconds;
		if (rpConfig == null) {
			try {
				dcemConfiguration = configLogic.getDcemConfiguration(getId(), AsConstants.RP_CONFIG_KEY);
			} catch (DcemException e1) {
				proxyCommClient.addReport(new RpReport(RpClientAction.Configuration, false, e1.toString()));
				return;
			}
			if (dcemConfiguration == null) {
				rpConfig = new RpConfig();
			} else {
				try {
					rpConfig = new ObjectMapper().readValue(dcemConfiguration.getValue(), RpConfig.class);
				} catch (Exception e) {
					proxyCommClient.addReport(new RpReport(RpClientAction.Initialize, false, e.toString()));
					return;
				}
			}
		}
		proxyCommClient.setRpConfig(rpConfig);
		if (rpConfig.enableRp == true) {
			if (reverseProxySchedule != null) {
				logger.info("reverseProxy schedule was running");
				reverseProxySchedule.cancel(true);
			}
			if (startImmediate) {
				startSeconds = 10;
			} else {
				startSeconds = rpConfig.getReconnect() * 60;
			}

			proxyCommClient.setState(ReverseProxyState.Initialized);
			reverseProxySchedule = taskExecutor.schedule(new ConnectDcemProxyTask(), startSeconds, TimeUnit.SECONDS);
		} else {
			proxyCommClient.setState(ReverseProxyState.No_Configuration);
			if (reverseProxySchedule != null) {
				reverseProxySchedule.cancel(true);
				reverseProxySchedule = null;
			}
			proxyCommClient.stop();
		}
	}

	public String getRealmName() {
		RpConfig rpConfig = proxyCommClient.getRpConfig();
		if (rpConfig.isEnableRp()) {
			return rpConfig.getDomainName();
		} else {
			return realmName; // The realmName of the Master Tenant
		}
	}

	public int getDbVersion() {
		return AS_DB_VERSION;
	}

	@Override
	public List<PolicyAppEntity> getPolicyApplications() {
		DcemRole role = roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_REST_SERVICE);
		List<DcemUser> restList = userLogic.getUsersFromRoles(Arrays.asList(role));
		List<AuthGatewayEntity> gatewayList = authGatewayLoigic.getAllAuthGateway();
		List<PolicyAppEntity> listPolicy = new ArrayList<>(restList.size() + gatewayList.size() + 3);
		listPolicy.add(new PolicyAppEntity(AuthApplication.DCEM, 0, null));
		listPolicy.add(new PolicyAppEntity(AuthApplication.WebServices, 0, null));
		for (DcemUser operator : restList) {
			listPolicy.add(new PolicyAppEntity(AuthApplication.WebServices, operator.getId(), operator.getLoginId()));
		}
		listPolicy.add(new PolicyAppEntity(AuthApplication.AuthGateway, 0, null));
		listPolicy.add(new PolicyAppEntity(AuthApplication.USER_PORTAL, 0, null));
		for (AuthGatewayEntity authGatewayEntity : gatewayList) {
			listPolicy.add(new PolicyAppEntity(AuthApplication.AuthGateway, authGatewayEntity.getId(), authGatewayEntity.getName()));
		}
		return listPolicy;
	}

	@Override
	@DcemTransactional
	public void deleteUserFromDb(DcemUser dcemUser) throws DcemException {

		activationLogic.deleteUserActivation(dcemUser);
		versionLogic.resetUser(dcemUser);
		messageLogic.deleteUserMsg(dcemUser);
		reportingLogic.deleteUserReports(dcemUser);
		cloudDataLogic.deleteAllUserRelatedData(dcemUser);
		deviceLogic.deleteUserDevices(dcemUser);
		asFidoLogic.deleteUserTokens(dcemUser);
		fingerprintLogic.deleteUserFingerprints(dcemUser.getId());
		DcemReporting reporting = new DcemReporting(ReportAction.DeleteUser, (DcemUser) null, null, null, dcemUser.getLoginId());
		reporting.setSource(AuthApplication.DCEM.name());
		reportingLogic.addReporting(reporting);
	}

	public AsTenantData getTenantData() {
		return (AsTenantData) getModuleTenantData();
	}

	public PolicyAppEntity getMainPolicyAppEntity(AuthApplication application) {
		AsTenantData tenantData = getTenantData();
		return tenantData.getMainPolicyApps().get(application);
	}

	@SuppressWarnings("unchecked")
	@DcemTransactional
	@Override
	public void initializeTenant(TenantEntity tenantEntity) throws DcemException {

		String tenantName = tenantEntity.getName();
		logger.debug("Start initializeTenant " + tenantName);
		AsTenantData tenantData = new AsTenantData();
		super.initializeTenant(tenantEntity, tenantData);
		DcemCluster dcemCluster = DcemCluster.getInstance();

		tenantData.setMainPolicyApps(policyLogic.getMainPolicies());

		IMap<Long, PendingMsg> pendingMsgs = (IMap<Long, PendingMsg>) dcemCluster.getMap("PendingMsgs@" + tenantName);
		pendingMsgs.addIndex("userId", false);
		pendingMsgs.addLocalEntryListener(new PendingMessageListener());
		tenantData.setPendingMsgs(pendingMsgs);

		FlakeIdGenerator msgIdGenerator = dcemCluster.getIdGenerator("messageIdGen@" + tenantName);

		tenantData.setMsgIdGenerator(msgIdGenerator);
		tenantData.setSmsPasscodesMap((IMap<FingerprintId, String>) dcemCluster.getMap("smsPasscodesMap@" + tenantName));
		tenantData.setLoginQrCodes((IMap<String, LoginQrCode>) dcemCluster.getMap("loginQrCodes@" + tenantName));

		try {
			PushNotificationConfig pushNotificationConfig = fcmLogic.loadConfiguration();
			fcmLogic.initialise(pushNotificationConfig);
			tenantData.setPushNotificationConfig(pushNotificationConfig);
		} catch (DcemException e) {
			logger.warn("Couldn't initialise FCM service on tenant " + tenantName + " Cause: " + e.toString());
		}

		policyLogic.syncPolicyAppEntity();
		try {
			policyLogic.createOrGetGlobalPolicy();
			policyLogic.createOrGetManagementPolicy();
			policyLogic.updatePolicyCache();
		} catch (Exception exp) {
			try {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't create policies", exp);
			} catch (DcemException e) {
				logger.error("Couldn't initialize Tenant: " + tenantName, e);
				throw e;
			}
		}
		try {
			deviceLogic.resetDevicesStatus(DcemCluster.getInstance().getDcemNode());
		} catch (Exception e) {
			logger.error("Couldn't reset devices for " + tenantName);
		}

		try {
			domainLogic.reload(null);
		} catch (Exception e) {
			logger.warn("Could initialize LDAP", e);
		}

	}

	public String getUserFullQualifiedId(DcemUser dcemUser) {
		String domain = getRealmName();
		String userName = dcemUser.getLoginId();
		if (dcemUser.isDomainUser()) {
			if (adminModule.getPreferences().isEnableUserDomainSearch() == true) {
				userName = dcemUser.getAccountName();
			} else if (dcemUser.getUserPrincipalName() != null) {
				userName = dcemUser.getUserPrincipalName();
			}
		}
		if (domain != null && domain.isEmpty() == false) {
			userName = userName + AppSystemConstants.REALM_SEPERATOR + getRealmName();
		}
		if (TenantIdResolver.isCurrentTenantMaster() == false) {
			userName = userName + AppSystemConstants.TENANT_SEPERATOR + TenantIdResolver.getCurrentTenantName();
		}
		return userName;
	}

	public boolean isEnableAppAutoVersionRegistration() {
		return enableAppAutoVersionRegistration;
	}

	public void setEnableAppAutoVersionRegistration(boolean enableAppAutoVersionRegistration) {
		this.enableAppAutoVersionRegistration = enableAppAutoVersionRegistration;
	}

	@Override
	@DcemTransactional
	public void deleteGroupFromDb(DcemGroup dcemGroup) throws DcemException {
		policyLogic.deletePolicesGroup(dcemGroup);
		cloudSafeLogic.deleteCloudSafeFileByOwnerGroup(dcemGroup);
		cloudSafeLogic.deleteShareGroupRelatedData(dcemGroup);
	}

	@Override
	public DcemAction getModuleAction() {
		return super.getModuleAction();
	}

	@Override
	public void setModuleAction(DcemAction moduleAction) {
		// TODO Auto-generated method stub
		super.setModuleAction(moduleAction);
	}

	@Override
	public boolean isPluginModule() {
		return false;
	}

}
