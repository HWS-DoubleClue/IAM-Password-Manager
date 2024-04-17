package com.doubleclue.dcem.as.logic;

public class AsConstants {

	public final static String CA_PRIVATE_KEY = "CA_PRIVATE_KEY";
	public final static String CA_PUBLIC_KEY = "CA_PUBLIC_KEY";
	public final static String CA_PUBLIC_CERTIFICATE = "CA_PUBLIC CERTIFICATE";
	public final static String CONNECTION_KEY = "CONNECTION_KEY";
	public final static String DCEM_AS_CA_ISSUER = "CN=DCEM_AS_CA_ISSUER, C=DE";
	// CN=www.mockserver.com, O=MockServer, L=London, ST=England, C=UK");

	public static final String ACTIVATION_DIALOG = "/modules/as/activationDialog.xhtml";
	public static final String VERSION_DIALOG = "/modules/as/versionDialog.xhtml";
	public static final String ACTION_GENERATE_SDK_CONFIG = "generateSdkConfiguration";
	public static final String GENERATE_SDK_CONFIG_DIALOG = "/modules/as/generateSdkConfigDialog.xhtml";
	public static final String ACTION_GOTO_CLOUDDATA = "gotoCloudData";

	public static final String ACTION_SEND_MESSAGE = "sendMessage";
	public static final String MESSAGE_DIALOG = "/modules/as/messageDialog.xhtml";
	public static final String PENDING_MSG_VIEW = "/modules/as/pendingMsgView.xhtml";
	public static final String REVERSE_PROXY_VIEW = "/modules/as/reverseProxyView.xhtml";
	public static final String TEST_USER_POLICY_DIALOG_PATH = "/modules/as/testUserPolicy.xhtml";

	public static final String MSG_SHOW_DIALOG = "/modules/as/msgShowDialog.xhtml";
	public static final String ACTION_DISPLAY_MSG = "actionDisplayMsg";
	public static final long MAX_WAIT_FOR_CLIENT_RESPONSE = 1000 * 30; // 30 seconds
	public static final long SESSION_TIMEOUT_AFTER_DISCONNECT = 1000 * 1; // 1 seconds
	public static final String ACTION_SHOW_ACTIVATION_CODE = "showActivationCode";
	public static final String SHOW_ACTIVATION_CODE_DIALOG_PATH = "/modules/as/showActivationCode.xhtml";
	public static final String ACTION_GOTO_ACTIVATIONCODE = "gotoActivationCodes";
	public static final String ACTION_GOTO_DEVICES = "gotoDevices";
	public static final String ACTION_ACTIVE_AUTH_GATEWAY = "activeAuthGateways";

	public static final String ICON_DISPLAY_BILLING = "fa fa-eye";

	public static final String ACTION_ARCHIVE = "actionArchive";
	public static final String DIALOG_ARCHIVE = "/modules/as/archive.xhtml";
	public static final String ICON_ARCHIVE = "archiveIcon";

	public static final long MAX_WAIT_RETRIEVE_PENDING_MSG = 1000 * 60 ; // 60 seconds

	public static final String EMAIL_ACTIVATION_CODE_KEY = "ActivationCode";
	public static final String EMAIL_ACTIVATION_VALID_TILL_KEY = "ValidTill";
	public static final String EMAIL_ACTIVATION_USER_KEY = "UserName";
	public static final String EMAIL_ACTIVATION_USER_DOMAIN_KEY = "UserDomain";
	public static final String USER_LOGINID = "UserLoginId";
	public static final String EMAIL_ACTIVATION_TENANT_URL = "TenantUrl";
	public static final String EMAIL_ACTIVATION_TENANT_NAME = "TenantName";
	public static final String EMAIL_ACTIVATION_TENANT_LOGIN_ID = "loginId";

	public static final String PASSWORD_BY_SMS = "Password";

	public final static String DISPATCHER_IMPL_CLASS = "dcemDispatcherImpl";

	public final static int WS_BUFFER_SIZE = 4 * 1024;
	public static final String DISPATCHER_PUBLIC_KEY = "/dispatcherPbKey.bin";

	public static final String DEFAULT_APP_NAME = "DoubleClue";
	public static final Object PROPERTY_PROXY_HOST = "proxyHost";
	public static final Object PROPERTY_PROXY_PORT = "proxyPort";
	public static final String DCEM_REVERSE_PROXY_FILE = "ReverseProxy.dcem";
	public static final String DCEM_REVERSE_PROXY_PROPERTY_FILE = "ReverseProxyProperties";

	public static final String SMS_ACTIVATION_BUNDLE_KEY = "as.SmsActivation";
	public static final String EMAIL_ACTIVATION_SUBJECT_BUNDLE_KEY = "as.EmailActivationSubject";

	public static final int REVERSE_PROXY_RECONNECT_TIME = 60; // seconds
	public static final String REVERSE_PROXY_DIALOG = "/modules/as/reverseProxyDialog.xhtml";
	public static final String RP_CONFIG_KEY = "rpConfig";
	public static final String RP_CONFIG_ENABLED = "ReverseProxy has been enabled";
	public static final String RP_CONFIG_DISABLED = "ReverseProxy has been disabled";
	public static final String TOKEN_WORKSTATTION = "workstation";

	public static final String ACTION_SHOW_CLOUD_SAFE_FILES = "actionShowCloudSafeFiles";
	public static final String PATH_SHOW_CLOUD_SAFE_FILES = "/modules/as/cloudSafeShowFilesDialog.xhtml";
	public static final String PATH_SET_CLOUD_SAFE_LIMITS = "/modules/as/cloudSafeSetLimitsDialog.xhtml";

	// LICENCE
	public static final int LICENCE_TRIAL_MAX_USERS = 100;
	public static final int LICENCE_TRIAL_MAX_STORAGE = 1024;
	public static final String POLICY_VIEW_PATH = "/modules/as/policies.xhtml";
	public static final String POLICY_ASSIGN_DIALOG_PATH = "/modules/as/assignPolicy.xhtml";
	public static final String POLICY_DIALOG_PATH = "/modules/as/policyDialog.xhtml";
	public static final String SMS_CODE = "smsCode";
	public static final String SMS_PASSCODE_BUNDLE_KEY = "as.SmsPasscode";
	public static final String SMS_USER_NAME = "UserName";
	public static final String VOICE_MESSAGE_BUNDLE_KEY = "as.VoiceMessage";
	public static final String EMAIL_ACTIVATION_IMAGE = "image";
	public static final String ACTION_PENDING_MESSAGES = "pendingMessages";
	public static final String PENDING_MSG_DIALOG = "/modules/as/pendingMsgDialog.xhtml";
	public static final String ACTIVE_AUTH_GATEWAY_DIALOG = "/modules/as/activeAuthGatewayDialog.xhtml";

	public static final String HAZELCAST_NAME_CLOUDSAFE_GLOBAL_USAGE = "cloudSafeGlobalUsage";
	public static final String EXTENSION_PASSWORD_SAFE = ".kdbx";
	public static final String BILLING_ITEM_PASSWORD_SAFE = "passSafe";

	// FIDO
	public static final String FIDO_DIALOG = "/modules/as/fidoAuthenticatorDialog.xhtml";
	public static final String ACTION_SHOW_RECOVERY_KEY = "actionShowRecoveryKey";
	public static final String RECOVERY_KEY_DIALOG = "/modules/as/recoveryKeyDialog.xhtml";
	
	// Push Notifications
	public static final String PUSH_NOTIFICATION_BODY_BUNDLE_KEY = "as.PushNotificationBody";
	public static final String PUSH_NOTIFICATION_ACTION_BUNDLE_KEY = "as.PushNotificationAction";
	public static final String PUSH_NOTIFICATION_TITLE_BUNDLE_KEY = "as.PushNotificationTitle";
	public static final String PUSH_NOTIFICATION_DIALOG = "/modules/as/pushNotificationDialog.xhtml";
	public static final String PUSH_NOTIFICATION_CONFIG_KEY = "FirebaseCM";
	public static final String APNS_EXPIRATION = "apns-expiration";
	public static final String FCM_FILE = "/fcm.enc";
	public static final String PASSWORD_BY_SMS_BUNDLE_KEY = "as.SmsPassword";
	
	// OTP
	public static final String TOTP_ALGORITHM_HMAC_SHA1 = "HmacSHA1";
	public static final String TOTP_ALGORITHM_HMAC_SHA256 = "HmacSHA256";
	public static final String TOTP_ALGORITHM_HMAC_SHA512 = "HmacSHA512";
	
	public final static String SHARE_BY_SEPERATOR = "@";
	public final static String SHARE_BY_GROUP_START = "(";
	public final static String SHARE_BY_GROUP_END = ")";
	public final static int LIB_VERION_2 = 0x200000;
}
