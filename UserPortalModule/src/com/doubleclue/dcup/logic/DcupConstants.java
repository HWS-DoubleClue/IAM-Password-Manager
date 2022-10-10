package com.doubleclue.dcup.logic;

public class DcupConstants {

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

	public static final int USER_PORTAL_DEFAULT_PORT = 443;
	// public static final String PATH_JSF_PAGES = "userportal";
	public static final String JSF_PAGE_LOGIN = "login.xhtml";
	public static final String JSF_PAGE_FORGOT_PASSWORD_REQUEST = "forgotPasswordRequestView_.xhtml";
	public static final String JSF_PAGE_FORGOT_PASSWORD = "forgotPasswordView_.xhtml";
	public static final String JSF_PAGE_END_MESSAGE = "endMessageView_.xhtml";
	public static final String JSF_NOTIFICATION_PAGE = "notificationView_.xhtml";
	public static final String SERVLET_NAME = "DCUP-Servlet";
	public static final String SERVLET_CLASS = "com.doubleclue.dcup.servlets.DcupServlet";
	public static final String WEB_USER_PORTAL_CONTEXT = "/userportal";
	public static final String PORTAL_WEB_NAME = "/dc";
	public static final String DCEM_WEB_NAME = "/dcem";
	public static final String DCEM_MGT_NAME = "/mgt";
	public static final String TYPE = "?type=";
	public static final String LOGIN_PAGE = "login.xhtml";
	public static final String WELCOME_PAGE = "welcome.xhtml";
	public static final String DCEM_PAGE = "/dcem/mgt/index.xhtml";
	public static final String REGISTER_PAGE = "register_.xhtml";
	public static final String REGISTER_LOCAL_USER = "registerLocalUser_.xhtml";
	public static final String JSF_PASS_THROUGH_PAGE_SUFFIX = "_.xhtml";
	public static final String FATAL_ERROR_PAGE = "error_.xhtml";
    public static final String REGISTER_DOM_USER = "registerDomUser_.xhtml";
    public static final String SUCCESS_REGISTRATION_PAGE = "successRegistrationView_.xhtml";
    public static final String CLOUD_SAFE_VIEW = "cloudSafeView.xhtml";
    public static final String PASSWORD_SAFE_VIEW = "keePassView.xhtml";
    public static final String DEVICES_VIEW = "devicesView.xhtml";
    public static final String USER_PROFILE_VIEW = "userProfileView.xhtml";
    public static final String CHANGE_PASSWORD_VIEW = "changePasswordView.xhtml";
    
    
	public static final String PORTAL_DIR = "/userportal";
	public static final String PORTAL_CONNECTION_CONFIG_FILE = "ConnectionConfig.json";
	public static final String PORTAL_TENANT_CONFIG_FILE = "TenantConfig.json";
	
	public static final String LS_PASSWORDSAFE_HISTORY = "'psHistory'";
	public static final String LS_LAST_VIEW = "'latestView'";
	public static final String LS_PASSWORDSAFE_PASSWORD = "'PasswordSafeEnc'";
	public static final String LS_PASSWORDSAFE_STATE = "'PasswordSafeState'";
	
	public static final String KEEPASS_PROPERTY_USER_NAME = "UserName";
	public static final String KEEPASS_PROPERTY_NOTES = "Notes";
	public static final String KEEPASS_PROPERTY_URL = "URL";
	public static final String KEEPASS_PROPERTY_PASSWORD = "Password";
	public static final String KEEPASS_PROPERTY_TITLE = "Title";
	public static final String HTML_PAGE_TUTORIAL = "tutorial_.xhtml";
	public static final String HTML_PAGE_TUTORIAL_DE = "tutorial_de_.xhtml";
	public static final String HTML_PAGE_USERSTORAGE = "userStorage_.xhtml";
	public static final String PRE_LOGIN_PAGE = "preLogin_.xhtml";

}
