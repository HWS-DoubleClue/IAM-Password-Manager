package com.doubleclue.dcem.as.logic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.tasks.ReloadClassInterface;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;

@ApplicationScoped
@Named("fcmLogic")
public class FcmLogic implements ReloadClassInterface {

	private static final Logger logger = LogManager.getLogger(FcmLogic.class);

	@Inject
	AsModule asModule;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	TemplateLogic tempalteLogic;

	@Inject
	ConfigLogic configLogic;

	private AndroidConfig androidConfig = null;

	public FcmLogic() {
	}

	public PushNotificationConfig loadConfiguration() {
		PushNotificationConfig pushNotificationConfig = null;
		try {
			DcemConfiguration dcemConfiguration = configLogic.getDcemConfiguration(asModule.getId(), AsConstants.PUSH_NOTIFICATION_CONFIG_KEY);
			if (dcemConfiguration == null) {
				pushNotificationConfig = new PushNotificationConfig(); // default
				pushNotificationConfig.setInherit(!TenantIdResolver.isCurrentTenantMaster());
			} else {
				pushNotificationConfig = new ObjectMapper().readValue(dcemConfiguration.getValue(), PushNotificationConfig.class);
			}
		} catch (Exception e) {
			logger.warn(e);
			pushNotificationConfig = new PushNotificationConfig(); // default
		}
		return pushNotificationConfig;
	}

	public void writeConfiguration(PushNotificationConfig pushNotificationConfig) throws DcemException {
		DcemConfiguration dcemConfiguration = configLogic.getDcemConfiguration(asModule.getId(), AsConstants.PUSH_NOTIFICATION_CONFIG_KEY);
		if (dcemConfiguration == null) {
			dcemConfiguration = new DcemConfiguration(asModule.getId(), AsConstants.PUSH_NOTIFICATION_CONFIG_KEY, null);
		}
		try {
			dcemConfiguration.setValue(new ObjectMapper().writeValueAsBytes(pushNotificationConfig));
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't save PN Config", e);
		}
		configLogic.setDcemConfiguration(dcemConfiguration);
		DcemUtils.reloadTaskNodes(FcmLogic.class, TenantIdResolver.getCurrentTenantName()); // inform all Nodes
	}

	/**
	 * @param pushNotificationConfig
	 * @throws DcemException
	 */
	public void initialise(PushNotificationConfig pushNotificationConfig) throws DcemException {
		String tenantName = TenantIdResolver.getCurrentTenantName();
		try {
			FirebaseApp firebaseApp = FirebaseApp.getInstance(tenantName); // remove App in case it is present
			firebaseApp.delete();
		} catch (Exception e) {
		}
		if (pushNotificationConfig.isEnable() == false || pushNotificationConfig.isInherit()) {
			return;
		}
		if (pushNotificationConfig.getGoogleServiceFile() == null) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't initilize FCM. No file available");
		}
		try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(pushNotificationConfig.getGoogleServiceFile().getBytes(DcemConstants.CHARSET_UTF8));
			FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(inputStream)).build();
			FirebaseApp.initializeApp(options, tenantName);
		} catch (IOException e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't initilize FCM.", e);
		}
	}

	/**
	 * @param registrationsIds
	 * @param dcemUser
	 * @throws DcemException
	 */
	public void pushNotification(Set<String> registrationsIds, DcemUser dcemUser, Boolean passwordLess) throws DcemException {
		try {
			PushNotificationConfig pushNotificationConfig = asModule.getTenantData().getPushNotificationConfig();
			if (pushNotificationConfig.isEnable() == false) {
				return;
			}
			String tenantName = TenantIdResolver.getCurrentTenantName();
			if (pushNotificationConfig.isInherit()) {
				tenantName = DcemConstants.MASTER_TENANT;
			}
			DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(dcemUser.getLanguage().getLocale());
			String title = dbResourceBundle.getString(AsConstants.PUSH_NOTIFICATION_TITLE_BUNDLE_KEY);
			String body = dbResourceBundle.getString(AsConstants.PUSH_NOTIFICATION_BODY_BUNDLE_KEY);
			String action = dbResourceBundle.getString(AsConstants.PUSH_NOTIFICATION_ACTION_BUNDLE_KEY);
			if (action == null) {
				action = "";
			}
			Map<String, String> map = new HashMap<>();
			map.put(AsConstants.EMAIL_ACTIVATION_USER_KEY, dcemUser.getDisplayNameOrLoginId());
			map.put(AsConstants.USER_LOGINID, dcemUser.getLoginId());
			String newBody = StringUtils.substituteTemplate(body, map);

			FirebaseApp firebaseApp = FirebaseApp.getInstance(tenantName); // remove App in case it is present
			// Notification notification = Notification.builder().setBody(newBody).setTitle(title).build();
			String userFqId = asModule.getUserFullQualifiedId(dcemUser);

			// Apple Push Notification Service (APNS)
			Map<String, Object> customData = new HashMap<String, Object>();
			customData.put(AppSystemConstants.UserFullQualifiedId, userFqId);
			customData.put(AppSystemConstants.PasswordLessLogin, passwordLess);
			ApnsConfig apnsConfig = getApnsConfig(title, newBody, customData);

			if (registrationsIds.size() == 1) {
				Message message = Message.builder()
						// .setAndroidConfig(getAndroidConfig())
						.setApnsConfig(apnsConfig).putData(AppSystemConstants.UserFullQualifiedId, userFqId)
						.putData(AppSystemConstants.PasswordLessLogin, passwordLess.toString()).putData(AppSystemConstants.PushNotificationTitle, title)
						.putData(AppSystemConstants.PushNotificationBody, newBody).putData(AppSystemConstants.PushNotificationAction, action)
						.setToken(registrationsIds.stream().findFirst().get()).build();
				try {
					String response = FirebaseMessaging.getInstance(firebaseApp).send(message);
					logger.debug("FCM User: " + userFqId + ", PN Response: " + response);
				} catch (FirebaseMessagingException e) {
					throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Coundn't send the message", e);
				}
			} else {
				MulticastMessage multiCastMessage = MulticastMessage.builder()
						// .setAndroidConfig(getAndroidConfig())
						.setApnsConfig(apnsConfig).putData(AppSystemConstants.UserFullQualifiedId, userFqId)
						.putData(AppSystemConstants.PasswordLessLogin, passwordLess.toString()).putData(AppSystemConstants.PushNotificationTitle, title)
						.putData(AppSystemConstants.PushNotificationBody, newBody).putData(AppSystemConstants.PushNotificationAction, action)
						.addAllTokens(registrationsIds).build();
				try {
					BatchResponse batchResponse = FirebaseMessaging.getInstance(firebaseApp).sendMulticast(multiCastMessage);
					logger.debug("FCM User: " + userFqId + ", PN Response: " + batchResponse);
				} catch (FirebaseMessagingException e) {
					throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Coundn't send the message", e);
				}
			}
		} catch (DcemException e) {
			throw e;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString());
		}
	}

	@Override
	public void reload(String info) throws DcemException {
		PushNotificationConfig pushNotificationConfig = loadConfiguration();
		initialise(pushNotificationConfig);
		if (asModule.getTenantData() != null) {
			asModule.getTenantData().setPushNotificationConfig(pushNotificationConfig);
		}
	}

	// private AndroidConfig getAndroidConfig() {
	// if (androidConfig == null) {
	// androidConfig = AndroidConfig.builder()
	// .setPriority(Priority.HIGH)
	// .setTtl(asModule.getPreferences().messageResponseTimeout)
	// .setNotification(
	// AndroidNotification.builder()
	// .setIcon("dc_logo_check_mark_web_rgb_trans")
	// .setPriority(AndroidNotification.Priority.HIGH)
	// .build())
	// .build();
	// }
	// return androidConfig;
	// }

	private ApnsConfig getApnsConfig(String title, String body, Map<String, Object> customData) {
		String expiration = "" + ((System.currentTimeMillis() / 1000L) + asModule.getPreferences().messageResponseTimeout);
		ApnsConfig.Builder builder = ApnsConfig.builder().putHeader(AsConstants.APNS_EXPIRATION, expiration)
				.setAps(Aps.builder().setMutableContent(true).setAlert(ApsAlert.builder().setTitle(title).setBody(body).build()).build());
		for (Map.Entry<String, Object> entry : customData.entrySet()) {
			builder.putCustomData(entry.getKey(), entry.getValue());
		}
		builder.putCustomData(AsConstants.APNS_EXPIRATION, expiration);
		return builder.build();
	}
}
