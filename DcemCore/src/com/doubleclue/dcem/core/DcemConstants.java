package com.doubleclue.dcem.core;

import java.nio.charset.Charset;

public final class DcemConstants {

	public static final String ISO_8859_1 = "ISO-8859-1";

	public static final String SUPPORTED_CIPHERS = "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, "
			+ "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, TLS_RSA_WITH_AES_256_CBC_SHA";

	public static final String SSL_PROTOCOLS = "TLSv1.2, TLSv1.3";

	public static final String LOG_FILE_NAME = "dcem.log";
	public static final String LOG_FILE_NAME_1 = "dcem-1.log";
	public static final String LOG_FILE_NAME_4 = "dcem-4.log";

	public static final String LOG_TOMCAT_FILE_NAME = "tomcat.0.log";
	public static final String LOG_TOMCAT_FILE_NAME_1 = "tomcat.1.log";
	public final static long MAX_FILE_LENGTH = (1024 * 1024) + 1; // 1 megabyte

	public static final String VERSION_DELIMITER = ".";
	public static final int VERSION_MAJOR_ABS = 255;
	public static final int VERSION_MINOR_ABS = 255;
	public static final int VERSION_RV_ABS = 1023;

	public static final int DEFAULT_KEY_PAIR_SIZE = 2048;

	public final static String AUTO_VIEW_PATH = "/autoView.xhtml";
	public final static String WELCOME_VIEW_PATH = "/welcome.xhtml";
	public final static String WELCOME_INDEX_PAGE = "index.xhtml";
	public final static String AUTO_DIALOG_PATH = "/autoDialog.xhtml";
	public final static String AUTO_CONFIRM_DIALOG_PATH = "/autoConfirmDialog.xhtml";
	public final static String PRIVILEGE_VIEW_PATH = "/modules/admin/privilege.xhtml";
	public static final String USER_VIEW_PATH = "/modules/admin/userDialog.xhtml";
	public static final String GROUP_DIALOG_PATH = "/modules/admin/groupDialog.xhtml";
	public static final String DOMAIN_DIALOG_PATH = "/modules/admin/domainDialog.xhtml";
	public static final String AZURE_MIGRATION_DIALOG_PATH = "/modules/admin/azureMigrationDialog.xhtml";

	public static final String SHOW_MEMBEROF_DIALOG = "/modules/admin/userMemberOfDialog.xhtml";
	public static final String GROUP_MEMBERS_DIALOG_PATH = "/modules/admin/groupMembersDialog.xhtml";
	public static final String DEPARTMENT_ORGAMIGRAM_DIALOG = "/modules/admin/departmentOrganigramDialog.xhtml";
	public static final String DEPARTMENT_DIALOG = "/modules/admin/departmentDialog.xhtml";;

	// public static final String SHOW_INITIAL_PASSWORD_DIALOG =
	// "/modules/admin/showInitialPassword.xhtml";

	public final static String PREFERENCES_VIEW_PATH = "/preferences.xhtml";
	public static final String TEXT_RESOURCE_VIEW_PATH = "/modules/admin/textResources.xhtml";
	public final static String TEST_DASHBOARD_VIEW_PATH = "/modules/test/dashboard.xhtml";

	public final static String ACTION_ADD = "add";
	public final static String ACTION_COPY = "copy";
	public final static String ACTION_EDIT = "edit";
	public final static String ACTION_DELETE = "delete";
	public final static String ACTION_VIEW = "view";
	public final static String ACTION_MOVE = "move";
	public final static String ACTION_CHANGE_OWNER = "change Owner";
	public final static String ACTION_MANAGE = "manage";
	public static final String ACTION_SAVE = "save";
	public static final String ACTION_DISABLE = "disable";
	public static final String ACTION_ENABLE = "enable";
	public static final String ACTION_PROPERTIES = "properties";
	public static final String ACTION_DETAILS = "details";
	public static final String ACTION_LOCK = "lock";
	public static final String ACTION_UNLOCK = "unlock";
	public static final String ACTION_SHOW = "show";
	public final static String ACTION_REST_API = "restWebServices";
	public static final String ACTION_SHOW_LICENCE = "showLicence";
	public final static String ACTION_IMPORT = "import";
	public static final String ACTION_ASSIGN = "assign";
	public static final String ACTION_UNASSIGN = "unassign";
	public static final String ACTION_MEMBER_OF = "memberOf";
	public static final String ACTION_RESET_PASSWORD = "resetPassword";
	public static final String ACTION_PUSH_NOTIFICATION = "configPushNotification";
	public static final String ACTION_SHOW_PN_TOKEN = "showPushNotificationToken";
	public final static String ACTION_EXCEL_EXPORT_ALL = "excelExportAll";
	public static final String ACTION_START = "start";
	public static final String ACTION_AZURE_MIGRATION = "azureMigration";

	public final static String AUTO_DIALOG_ID = "AUTO_DIALOG";

	public static final String TEMPLATE_DIALOG = "/modules/admin/templateDialog.xhtml";
	public static final String TEXT_RESOURCE_DIALOG = "/modules/admin/textResourceDialog.xhtml";
	public static final String TEXT_RESOURCE_UPLOAD_DIALOG = "/modules/admin/textResourceUploadDialog.xhtml";
	public static final String TEXT_RESOURCE_DOWNLOAD_DIALOG = "/modules/admin/textResourceDownloadDialog.xhtml";

	public final static String ACTION_ADD_ICON = "fa fa-plus";
	public static final String ACTION_COPY_ICON = "fa fa-copy";
	public final static String ACTION_EDIT_ICON = "fa fa-edit";
	public final static String ACTION_DELETE_ICON = "fa fa-close";
	public static final String ACTION_DISABLE_ICON = "fa fa-minus";
	public static final String ACTION_ENABLE_ICON = "fa fa-check";
	public static final String ACTION_UPLOAD_ICON = "fa fa-upload";
	public static final String ACTION_DOWNLOAD_ICON = "fa fa-download";
	public static final String ACTION_SAVE_ICON = "fa fa-save";

	public static final String ACTION_EXPORT = "export";
	public static final String ACTION_PREDEFINED_FILTERS = "predefinedFilters";
	public static final String ACTION_FEATURES = "features";
	public static final String ACTION_DOCUMENTS = "documents";
	public static final String ACTION_CLEAR = "Clear";
	public static final String ACTION_DOWNLOAD = "download";
	public static final String ACTION_GENERATE = "generate";
	public static final String ACTION_UPLOAD = "upload";
	public static final String ACTION_SHOW_PASSWORD = "showPassword";
	public static final String ACTION_RUN = "run";
	public static final String ACTION_STOP = "stop";
	public static final String ACTION_DOWNLOAD_PK12 = "downloadPk12";
	public static final String ACTION_DOWNLOAD_PEM = "downloadPem";
	public static final String ACTION_CLEAR_LOG = "clearLog";
	public static final String ACTION_SHOW_REPORT = "showReport";

	public static final String ACTION_IMPORT_LICENCE_KEY = "importLicenceKey";
	public static final String TEXT_RESOURCE_FILE_TYPE = ".properties";
	public static final char TEXT_RESOURCE_FILE_SEPERATOR = '_';

	/**
	 * Global ACTIONS
	 */

	public final static String CONFIG_KEY_CLUSTER_CONFIG = "CLUSTER_CONFIG";
	public final static String CONFIG_KEY_PREFERENCES = "preferences";
	public static final String CORE_RESOURCE = "com.doubleclue.dcem.core.resources.Messages";
	public static final String VALIDATION_CONSTRANT = "violation.constrain";
	public static final String AUTO_CONFIRM_DIALOG = "AUTO_CONFIRM_DIALOG";
	public static final String DELETE_CONFIRMATION_MSG = "auto.delete.confirm.message";

	public static int WEB_MANAGEMENT_PORT = 8443;
	public static String APP_TITLE = "Enterprise Management";
	public static String USERPORTAL_TITLE = "UserPortal";
	public static final String SYSTEM_ROLE_SUPERADMIN = "SuperAdmin";
	public static final String SYSTEM_ROLE_ADMIN = "Admin";
	public static final String SYSTEM_ROLE_HELPDESK = "HelpDesk";
	public static final String SYSTEM_ROLE_VIEWER = "Viewer";
	public static final String SYSTEM_ROLE_REST_SERVICE = "RestServices";
	public static final String SYSTEM_ROLE_USER = "User";

	public static final String EMPTY_SUBJECT_NAME = "-";
	public static final char JPA_ESCAPE_CHAR = '/';
	public static final String JPA_ESCAPE_CHAR_QUOTES = "'/'";
	public static final String PASSWORD_REPLACEMENT = "XxXxXxXxXx";
	public static final String TABLE_STYLE = "autoTableStyle";
	public static final String ACTION_RECOVER_SUPERADMIN_ACCESS = "recoverSuperAdminAccess";
	public static final String ACTION_SWITCH_TO_TENANT = "switchToTenant";

	public static final String RESET_PASSWORD_DIALOG_PATH = "/modules/admin/resetPasswordDialog.xhtml";
	public static final Object VIEW_SUFFEIX = "View";

	public static final String CHARSET_UTF8 = "UTF-8";

	public static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

	public static final String DEFAULT_WEB_NAME = "/dcem";
	public static final int DEFAULT_SCALE_FACTOR = 20;

	public static final int DEFAULT_MGT_PORT = 8443;
	public static final int DEFAULT_REST_API_PORT = 8082;
	public static final int DEFAULT_WEBSOCKETS_PORT = 8081;

	public static final String WEB_MGT_CONTEXT = "/mgt";

	public static final String CLUSTER_CONFIG_VIEW_PATH = "/modules/system/clusterConfig.xhtml";
	public static final String SHOW_PASSWORD_DIALOG_PATH = "/modules/system/showPassword.xhtml";
	public static final String GENERATE_KEYSTORE_DIALOG_PATH = "/modules/system/generateKeyStore.xhtml";
	public static final String UPLOAD_KEYSTORE_DIALOG_PATH = "/modules/system/uploadKeyStore.xhtml";
	public static final String DIAGNOSTICS_VIEW_PATH = "/modules/system/diagnostics.xhtml";
	public static final String LICENCE_VIEW_PATH = "/modules/admin/licence.xhtml";
	public static final String LICENCE_DIALOG_PATH = "/modules/admin/licenceDialog.xhtml";
	public static final String IMPORT_USERS_VIEW_PATH = "/modules/admin/importLdapUsers.xhtml";
	public static final String SHOW_ACTIVATION_CODE_DIALOG = "/modules/admin/adminActivationDialog.xhtml";
	public static final String USER_PORTAL_WELCOME = "/dcem/userportal/welcome.xhtml";

	public static final String SYSTEM_TENANT_DIALOG_PATH = "/modules/system/tenantDialog.xhtml";
	public static final String TENANT_RECOVER_SUPERADMIN_ACCESS_DIALOG_PATH = "/modules/system/tenantRecoverSuperAdminAccessDialog.xhtml";

	public static final String NODE_NAME_ATTRIBUTE = "nodeName";

	public static final String CONFIG_KEY_DB_VERIFICATION = "DB_VERIFICATION";

	/*
	 * 
	 * Key Generation
	 */
	public static final String DB_KEY_ALGORITHM = "AES";
	public static final String DB_KEY_CRYPTO_MODE = "CBC";
	public static final String DB_KEY_PADDING = "PKCS5Padding";
	public static final String DB_KEY_ALG_MODE = "AES/CBC/PKCS5Padding";
	public static final int DB_KEY_LENGTH = 256;

	public static final String SUBJECT_REST_API = "REST-API";
	public static final Object KEY_STORE_TYPE = "pkcs12";

	public static final String CLUSTER_CONFIG_PATH = "com/doubleclue/dcem/core/config/HazelcastClusterConfig.xml";
	public static final String CLUSTER_CONFIG_NAME_X = "x_HazelcastClusterConfig.xml";
	public static final String CLUSTER_INTANCE_NAME = "MainCluster";

	public static final int MAX_DB_RESULTS = 2000;
	public static final String REST_API_OPERATOR = "restservicesoperator";
	public static final String REST_API_OPERATOR_DISPLAY = "REST Services Operator";
	public static final String EMBEDDED_USER_PORTAL_OPERATOR = "embeddedupo";
	public static final String SUPER_ADMIN_OPERATOR = "superadmin";
	public static final String SUPER_ADMIN_OPERATOR_DISPLAY = "Super Admin Operator";

	public static final String ACTION_RESET_COUNTERS = "resetDiagCounters";
	public static final String ACTION_DOWNLOAD_DIAGNOSTIC_FILE = "downloadDiagFile";
	public static final String ACTION_DOWNLOAD_LOG_FILE = "downloadLogFile";
	public static final String ACTION_SHOW_DIAGNOSTIC_CHARTS = "showDiagnosticCharts";
	public static final String ACTION_CONFIGURE = "configure";
	public static final String ACTION_TEST_USER_POLICY = "testUserPolicy";
	public static final String ACTION_RESET_STAY_LOGIN = "resetStayLogin";
	public static final String ACTION_RECOVER_TEMPLATES = "recoverTemplates";

	public static final String CONFIG_KEY_LICENCE = "LICENCE_KEY";

	// LICENCE
	public static final int LICENCE_ACTIVATION_EXPIRY_GRACE_PERIOD_DAYS = 30;
	public static final String EMAIL_LICENCE_WARNING_BODY_TEMPLATE = "EmailLicenceWarningBody";
	public static final String EMAIL_LICENCE_USER_KEY = "UserName";
	public static final String EMAIL_LICENCE_WARNING_KEY = "LicenceWarnings";

	public final static String DISPATCHER_MODULE_ID = "dispatcher";
	public final static String OTP_MODULE_ID = "otp";
	public final static String AS_MODULE_ID = "as";
	public final static String USER_PORTAL_MODULE_ID = "up";
	public final static String PERFORMANCE_DECK_MODULE_ID = "pd";

	public static final String AS_MODULE_ACTIVATION_LOGIC = "asActivtionLogic";

	public static final String RADIUS_OPERATOR_NAME = "radiusoperator";
	public static final String EMAIL_ACTIVATION_BODY_TEMPLATE = "as.EmailActivationBody";
	public static final String EMAIL_ACTIVATION_NEW_TENANT_TEMPLATE = "as.EmailActivationNewTenant";
	public static final String EMAIL_RESET_PASSWORD_BODY_TEMPLATE = "system.EmailForgotPasswordBody";
	public static final String EMAIL_VERIFY_EMAIL_BODY_TEMPLATE = "system.EmailVerifyEmailBody";

	public static final String EMAIL_ACTIVATION_SELF_CREATE_TENANT_TEMPLATE = "licence.EmailActivationSelfCreateTenant";

	public final static String RADIUS_LOGIN_TEMPLATE = "radius.Login";
	public final static String RADIUS_LOGIN_CHALENGE_TEMPLATE = "radius.LoginChallenge";

	public static final String SPECIAL_PROPERTY_RUN_NIGHTLY_TASK = "runNightlyTask";
	public static final String SPECIAL_PROPERTY_LOG_DB_STATISTICS = "logDbStatistics";

	public static final String EMAIL_QUARTERLY_BILLING_CLUSTER_KEY = "ClusterKey";
	public static final String EMAIL_QUARTERLY_BILLING_TENANT_KEY = "TenantKey";

	// SAML
	public static final String SAML_SERVLET_CLASS = "com.doubleclue.dcem.saml.servlets.SamlServlet";
	public static final String SAML_SERVLET_NAME = "samlIdp";
	public static final String SAML_SERVLET_PATH = "/saml";
	public static final String SAML_OPERATOR_NAME = "SamlOperator";
	public static final String SAML_SERVLET_FILTER_NAME = "SamlWebFilter";
	public static final String SAML_SERVLET_FILTER_CLASS = "com.doubleclue.dcem.saml.servlets.SamlWebFilter";

	public static final String WEBDAV_SERVLET_NAME = "WebDAV";
	public static final String WEBDAV_SERVLET_PATH = "/webdav/*";
	public static final String WEBDAV_SERVLET_CLASS = "com.doubleclue.dcem.userportal.servlets.DcWebDavServlet";

	// OAuth
	public static final String OAUTH_SERVLET_CLASS = "com.doubleclue.dcem.oauth.servlets.OAuthServlet";
	public static final String OAUTH_SERVLET_NAME = "oauthAuthServer";
	public static final String OAUTH_SERVLET_PATH = "/oauth";
	public static final String OAUTH_OPERATOR_NAME = "OAuthOperator";
	public static final String OAUTH_SERVLET_FILTER_NAME = "OAuthWebFilter";
	public static final String OAUTH_SERVLET_FILTER_CLASS = "com.doubleclue.dcem.oauth.servlets.OAuthWebFilter";

	// HEALTH CHECK
	public static final String HEALTHCHECK_SERVLET_CLASS = "com.doubleclue.dcem.core.servlets.HealthCheckServlet";
	public static final String HEALTHCHECK_SERVLET_NAME = "HealthCheckServlet";
	public static final String HEALTHCHECK_SERVLET_PATH = "/healthcheck";

	// TEST SP
	public static final String TEST_SP_SERVLET_CLASS = "com.doubleclue.dcem.test.servlet.TestSpServlet";
	public static final String TEST_SP_SERVLET_NAME = "testSp";
	public static final String TEST_SP_SERVLET_PATH = "/testmodule/acs";

	// TestModule
	public static final String TESTMODULE_SERVLET_FILTER_NAME = "testservice";
	public static final String TESTMODULE_SERVLET_FILTER_CLASS = "com.doubleclue.dcem.test.servlet.TestModuleFilter";
	public static final String TESTMODULE_SERVLET_FILTER = "/testservice/*";

	// Licence
	public static final String LICENCE_SERVLET_CLASS = "com.doubleclue.dcem.licence.servlets.LicenceServlet";
	public static final String LICENCE_SERVLET_NAME = "LicenceServlet";
	public static final String LICENCE_SERVLET_PATH = "/lcServlet";

	// user portal
	public static final String USERPORTAL_SERVLET_CLASS = "com.doubleclue.dcem.userportal.servlets.DcupServlet";
	public static final String USERPORTAL_SERVLET_NAME = "DcupServlet";
	public static final String USERPORTAL_SERVLET_FILTER = "/userportal/*";
	public static final String USERPORTAL_SERVLET_FILTER_CLASS = "com.doubleclue.dcem.userportal.servlets.EmbeddedUserPortalFilter";
	public static final String USERPORTAL_SERVLET_FILTER_NAME = "EmbeddedUserPortalFilter";
	public static final String USERPORTAL_SERVLET_PATH = "/userPortalServlet";
	public static final String USERPORTAL_SERVL_PATH = "userPortalServlet";
	public static final String DCDB_URL_PROTOCOL = "dcdb";
	public static final String EMAIL_LICENCE_WARNING_SUBJECT_BUNDLE_KEY = "system.EmailLicenceWarningSubject";
	public static final String EMAIL_PASSWORD_RESET_SUBJECT_BUNDLE_KEY = "system.EmailForgotPasswordSubject";
	public static final String EMAIL_VERIFY_EMAIL_SUBJECT_BUNDLE_KEY = "system.EmailVerifyEmailSubject";
	public static final String EMAIL_FORGOT_PASSWORD_KEY = "ResetLink";
	public static final String SNAPSHOT_VERSION = "-SNAPSHOT";
	public static final String DCEM_MODULE_ID = "DCEM";
	public static final String DOMAIN_SEPERATOR = "\\";
	public static final String DOMAIN_SEPERATOR_REGEX = "\\\\";
	public static final String ACTION_MEMBERS = "members";
	public static final String GLOBAL_POLICY = "Global-Policy";

	public static final String MANAGEMENT_POLICY = "DCEM-Management";
	public static final String TYPE_DOMAIN = "domain";
	public static final String TYPE_LOCAL = "local";

	public static final int MAX_ARCHIVE_RECORDS = 1000;
	public static final String LDAP_DISPLAY_ATTRIBUTE = "displayName";
	public static final String LDAP_PREFERRED_LANGUAGE = "preferredLanguage";
	public final static String TEMPLATE_TYPE = ".html";
	public final static String MYAPPLICATIONS_TYPE = ".dcMyApp";
	public final static String TEMPLATE_RESOURCES = "com/doubleclue/dcem/templates";
	public final static String MYAPPLICATIONS_RESOURCES = "com/doubleclue/dcem/myApplications";
	public static final String TEXT_RESOURCES_FOLDER = "com/doubleclue/dcem/text/";
	public static final String TEXT_TYPE = ".properties";
	public static final String LOCAL_DOMAIN = ".\\";
	public static final String MDC_TENANT = "tenant";
	public static final String MDC_USER_ID = "userId";
	public static final String URL_TENANT_PARAMETER = "tenant";
	public static final String URL_TENANT_SWITCH = "tenantSwitch";
	public static final String CREATE_ACTIVATION_CODE = "createActivationCode";
	public static final Charset UTF_8 = Charset.forName(CHARSET_UTF8);
	public static final String AS_MODULE_API_IMPL_BEAN = "asModuleApiImpl";

	// public static final String AUTH_PARAM_NETWORK = "network";
	// public static final String AUTH_PARAM_FINGERPRINT = "fingerprint";
	// public static final String AUTH_PARAM_TEMPLATE_NAME = "template";
	// public static final String AUTH_PARAM_IGNORE_PASSWORD = "ignorePassword";
	// public static final String AUTH_PARAM_DATA_MAP = "dataMap";
	// public static final String AUTH_PARAM_USER = "user";
	public static final String AUTH_MAP_CODE = "code";
	public static final String AUTH_MAP_SOURCE = "source";
	// public static final String AUTH_PARAM_SESSION_ID = "SessionId";
	// public static final String AUTH_PARAM_REPORT_INFO = "reportInfo";
	// public static final String AUTH_PARAM_FIDO_RESPONSE = "fidoResponse";
	// public static final String AUTH_PARAM_FIDO_RP_ID = "fidoRpId";
	// public static final String AUTH_PRAM_USE_ALTERNATIVES = "user'Alternatives";

	public static final String MESSAGE_WRONG_CREDENTIALS = "operatorLogin.wrong_credentials";
	public static final String MESSAGE_RESET_PASSWORD_BEFORE_LOGIN = "operatorLogin.reset_password_before_login";

	public static final Object OK = "OK";

	// FIDO
	public static final String FIDO_PARAM_JSON = "json";
	public static final String FIDO_ERROR_ABORTED_CHROME = "The operation either timed out or was not allowed. See: https://w3c.github.io/webauthn/#sec-assertion-privacy.";
	public static final String FIDO_ERROR_ABORTED_FIREFOX = "The operation was aborted. ";
	public static final String FIDO_ERROR_NO_AUTHENTICATORS_CHROME = "Resident credentials or empty 'allowCredentials' lists are not supported at this time.";
	public static final String FIDO_ERROR_NOT_REGISTERED_CHROME = "The user attempted to use an authenticator that recognized none of the provided credentials.";
	public static final String FIDO_ERROR_NOT_REGISTERED_FIREFOX = "An attempt was made to use an object that is not, or is no longer, usable";
	public static final String FIDO_ERROR_ALREADY_REGISTERED_CHROME = "The user attempted to register an authenticator that contains one of the credentials already registered with the relying party.";
	public static final String FIDO_ERROR_ALREADY_REGISTERED_FIREFOX = "An attempt was made to use an object that is not, or is no longer, usable";
	public static final String FIDO_ERROR_WRONG_RP_ID_FIREFOX = "The operation is insecure.";

	// public static final String USER_PORTAL_SERVLET_PATH = "/openServlet";
	public static final String USER_PORTAL_SERVLET_NAME = "DCUP-Servlet";
	public static final String USER_PORTAL_CONTEXT = "/userportal";
	public static final String EXPIRED_PAGE = "expired_.xhtml";
	public static final String LOGOFF_PAGE = "logoff_.xhtml";

	// THEME
	public static final String DCEM_PORTAL_THEME = "dcemportaltheme";
	public static final String DCEM_THEME = "dcemtheme";
	public static final String EMBEDDED_USER_PORTAL_PATH = "/dcem/userportal";

	// AZURE
	public static final String AZURE_CALLBACK_SERVLET_NAME = "AzureCallbackServlet";
	public static final String AZURE_CALLBACK_SERVLET_PATH = "/azureCallback";
	public static final String AZURE_AUTHORITY = "https://login.microsoftonline.com/";
	public static final String AZURE_RESOURCE_GRAPH = "https://graph.microsoft.com/";

	public static final int MAX_MEM_CLOUD_SAFE_LENGTH = (1024 * 1024 * 1024); // 1 Gbyte
	public static final String FACES_REDIRECT = "?faces-redirect=true";
	public final static int LOGIN_POLL_INTERVAL_MILLI_SECONDS = 4000;
	public final static int LOGIN_WAIT_INTERVAL_MILLI_SECONDS = LOGIN_POLL_INTERVAL_MILLI_SECONDS - 260;

	public static final String RECOVERY_KEY = "RecoveryKey";
	public static final String regxTelefonNumber = "([\\+(]?(\\d){2,}[)]?[- \\.]?(\\d){2,}[- \\.]?(\\d){2,}[- \\.]?(\\d){2,}[- \\.]?(\\d){2,})|([\\+(]?(\\d){2,}[)]?[- \\.]?(\\d){2,}[- \\.]?(\\d){2,}[- \\.]?(\\d){2,})|([\\+(]?(\\d){2,}[)]?[- \\.]?(\\d){2,}[- \\.]?(\\d){2,})";
	public static final String MASTER_TENANT = "master";

	public static final String PRE_LOGIN_PAGE = "preLogin_.xhtml";

	public static final String PARAM_IGNORE_DB_VERSION = "ignoreDbVersion";

	public static final String HTML_PAGE_PRE_LOGIN = "preLogin_.xhtml";
	public static final String HTML_PAGE_LOGIN = "login.xhtml";
	public static final String HTML_PAGE_SELECT_LOGIN = "selectLogin_.xhtml";

	public static final String DEFAULT_FILE_ICON = "file.png";

	public static final String DEFAULT_BIN_ICON = "bin.png";

	public static final String DEFAULT_FOLDER_ICON = "folder.png";

	public static final String DEFAULT_FOLDER_LOOK_ICON = "folder_lock.png";

	public static final String CONFIG_KEY_TENANT_BRANDING = "tenantBranding";

	public static final String LDAP_URL_TYPE = "ldaps://";

	public static final Object SESSION_TIMEZONE = "Timezone";

	public static final String HTTPS_PROTOCOL = "https";
	public static final String SECURE_WEB_SCOKET_PROTOCOL = "wss://";
	public static final String HTTPS_PROTOCOL_FULL = "https://";

	public static final String CONFIG_KEY_SDK_CONFIG = "SdkConfig";

	public static final Integer FINGERPRINT_ID_FOR_APP = -2;

	public static final String EMAIL_ALERTS_BODY_TEMPLATE = "system.EmailAlertsBody";
	public static final String EMAIL_ALERTS_SUBJECT_BUNDLE_KEY = "system.EmailAlertsSubject";
	public static final String EMAIL_ALERTS_ALERT_MESSAGE = "AlertMessage";

	public static final String MAIL_ALERT_TITLE = "Mail Server";

	public static final String REPORT_ERROR_CODE_DENY = "Deny";

	public static final String DOUBLECLUE = "Doubleclue";

	public static final String PREF_HELP_RESOURCE = "help";

	public static final String ALERT_CATEGORY_DCEM = "dcem";

	public final static int MAX_CIPHER_BUFFER = 1024 * 64;

	public static final int SERVER_LOGIC_EXCEPTION = 599;

	public final static String MODULE_VIEW_SPLITTER = ".";

	public final static String GROUP_VIEW = "Group";

	public final static String DAY_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
	public final static String DAY_FORMAT = "yyyy-MM-dd";

	public final static String MANUALS_URL_LOCATION = "../../../Manuals/DCEM_Manual_";

	public final static String MANUALS_FILE_LOCATION = "/Manuals/DCEM_Manual_";

	public final static String PDF_EXT = ".pdf";

	public final static String CLOUD_SAFE_RECYCLE_BIN = "Recycle Bin";

	public final static String CLOUD_SAFE_ROOT = "_ROOT_";

	public final static String DEVICE_ROOT = "_DEVICE_ROOT_";

	public final static String GROUP_ROOT = "_GROUP_ROOT_";

	public static final String USER_AGENT = "user-agent";

	public static final String CHROME_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36";

	public static final String DOWNLOAD_CIPHER_EXCEPTION = "DOWNLOAD_CIPHER_EXP";

	public static final String USERPORTAL_PREFERNCESES_TYPE_COMPARE = "notificationType";

	public static final String EMAIL_STATE_SUCCESSED = "succssed";

	public static final String EMAIL_STATE_FAILED = "failed";

	public static final String OBJECT_IDENTIFIER = "objectIdentifier";

	public static final String EMAIL_VERIFICATION_STATE = "EmailState";

	public static final int LICENCE_TRIAL_EXPIRY_DAYS = 30;

	public static final int LICENCE_MAX_DEFAULT_USERS = 100;

	public static final int LICENCE_KEY_VERSION = 2;

	public static final String USER_DEFAULT_PROFILE_PHOTO_PATH = "/com/doubleclue/dcem/images/user.png";

	public static final String ACTION_GENERATE_SHIFTS_ENTRY = "generateShiftsEntry";
	public static final String SHIFTS_NEW_ASSIGNMENT_BODY_TEMPLATE = "shifts.NewAssignmentBody";
	public static final String SHIFTS_ASSIGNMENT_REMOVED_BODY_TEMPLATE = "shifts.AssignmentRemovedBody";
	public static final String EMAIL_USER_KEY = "UserName";
	public static final String EMAIL_SHIFT_NAME = "shift";
	public static final String ACTION_BY_USER = "actionByUser";
	public static final String EMAIL_SHIFT_DATE = "shiftDate";
	public static final String EMAIL_SHIFT_SUBJECT_ASSGINED = "Shift has been assigned";
	public static final String EMAIL_SHIFT_SUBJECT_REMOVED = "Shift has been removed";

	public static final String DEFAULT_APP_ICON = "/appHub/DC_Logo_transp_01.2.png";

	public static int PHOTO_WIDTH = 128;
	public static int PHOTO_HEIGHT = 128;
	public static int PHOTO_WIDTH_MIN = 64;
	public static int PHOTO_HEIGHT_MIN = 64;
	public static int PHOTO_MAX = 8096 * 2;
	public static int IMAGE_MAX = 32000;

	public static final String COUNTRY_CODE_GERMAN = "DE";
	public static final String COUNTRY_CODE_MALTA = "MT";
	public static final String COUNTRY_CODE_INDIA = "IN";

	public static final String ACTION_ORGANIGRAM = "organigram";
	public static final String ACTION_REVEAL = "reveal";
	public static final String METHOD_IS_RESTRICTED = "isRestricted";
	public static final String RESTRICTED_REPLACEMENT = "-----";

	public static final String SYSTEM_DEFAULT_ZONE = "Europe/Berlin";
	public static final String TIME_ZONE_OTHER = "Other...";

	public static final String SESSION_LOCALE = "Locale";

}
