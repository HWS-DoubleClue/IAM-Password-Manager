package com.doubleclue.dcem.core.exceptions;

public enum DcemErrorCodes {

	WRITE_CONFIG_FILE,
	CONFIGURE_HOME_DIRECTORY,
	CONFIGURE_INSTALL_DIRECTORY,
	INVALID_DB_TYPE,
	INIT_DATABASE,
	INIT_APPLICATION,
	STARTING_MODULE,
	NO_CONNECTION_CONFIG,
	INVALID_DB_ENCRYPTION_KEY,
	NO_DB_ENCRYPTION_KEY,
	CREATE_MODULE_ACTION,
	READ_CONFIG,
	UNSUPPORTED_FILTER,
	NO_DOMAIN_ENTITIES,
	LDAP_LOGIN_SEARCH_ACCOUNT_FAILED,
	LDAP_SEARCH_USER_FAILED,
	LDAP_LOGIN_USER_FAILED,
	LDAP_RETRIEVE_ATTRIBUTES,
	LDAP_CONNECTION_FAILED,
	NO_CONFIG_FILE_FOUND,
	TEMPLATE_BAD_FORMAT,
	TEMPLATE_VALUE_MISSING,
	DOMAIN_WRONG_AUTHENTICATION,
	NO_TEMPLATE_FOUND,
	UNEXPECTED_ERROR,
	ROLE_RESTRICTIOIN_EMPTY_FILTER,
	INVALID_VERSION_SYNTAX,
	CANNOT_CHANGE_TEMPLATE_IN_USE,
	USER_IS_NULL,
	EXCEED_USER_MESSAGE_QUEUE,
	EXCEPTION,
	RESERVED,
	USER_DISABLED,
	USER_HAS_NO_DEVICES,
	MESSAGE_NOT_FOUND,
	QRCODE_GENERATION_FAILED,
	LOGIN_QR_CODE_NOT_FOUND,
	LOGIN_QR_CODE_FAILED,
	LOGIN_QR_CODE_CONSUMED,
	LOGIN_QR_CODE_INVALID,
	INVALID_OTP,
	CLUSTER_CONFIG_FILE_NOT_FOUND,
	SERIALIZATION_ERROR,
	NODE_NAME_NOT_FOUND,
	CLUSTER_NODE_NOT_CONFIGURED,
	CLUSTER_INIT_FAILED,
	INVALID_CLOUDDATA_OWNER,
	INVALID_CLOUDDATA_USER,
	INVALID_CLOUDDATA_DEVICE,
	INVALID_CLOUDDATA_NAME,
	CREATE_KEYSTORE_FOR_NODES,
	CANNOT_CREATE_TABLES,
	INVALID_FILTER_NAME,
	CONSTRAIN_VIOLATION_DB,
	CONSTRAIN_VIOLATION,
	DB_TRANSACTION_ERROR,
	COMPARE_OBJECTS,
	INVALID_USERID,
	NO_DEFAULT_TEMPLATE_FOUND,
	OUTPUT_DATA_CONVERTION_FAILED,
	DEVICE_NOT_FOUND,
	CLOUD_SAFE_NOT_FOUND,
	MAX_WAIT_RETRIEVE_PENDING_MSG,
	USER_PASSWORD_MAX_RETRIES,
	PASSCODE_NOT_NUMERIC,
	INVALID_PASSWORD,
	EMAIL_CONNECTION_FAILED,
	EMAIL_MESSAGE_FAILED,
	EMAIL_SEND_MSG_FAILED,
	EMAIL_NOT_CONFIGURED,
	EMAIL_INVALID_CONFIGURATION,
	SMS_INVALID_CONFIGURATION,
	SMS_USER_HAS_NO_MOBILE,
	SMS_SEND_EXCEPTION,
	SMS_UNAUTHORIZED,
	EMAIL_AUTHENTICATION_FAILED,
	USER_EXISTS_ALREADY,
	INVALID_CONFIGURATION_FILE,
	INVALID_LICENCE_LIMITATIONS,
	INVALID_LICENCE_CONTENT,
	LICENCE_MAX_USER,
	LICENCE_EXPIRED,
	ID_WITH_SPECIAL_CHARACTERS,
	TABLE_EXISTS_ALREADY,
	LDAP_MAIN_CONTEXT_FAILED,
	RESERVED_2,  // 
	DISPATCHER_KEY_MISSING,
	COULD_NOT_REGISTER_DCEM,
	REVERSE_PROXY_FILE_NOT_FOUND,
	INVALID_PATH_EMBEDDED_DATABASE_BACKUP,
	INVALID_SPECIAL_PROPERTIES_SYNTAX,
	INVALID_SDK_CONFIG,
	INVALID_RP_CONFIG,
	INVALID_SAML_METADATA,
	MISSING_SAML_PREFERENCE,
	SECURE_CONNECTION_UNTRUSTED,
	SECURE_CONNECTION_INVALID_HOST_NAME,
	SECURE_CONNECTION_FAIL,
	CONNECTION_FAIL,
	SERVER_RESPONSE_ERROR,
	WRONG_SERVER_SIGNATURE,
	CANNOT_MIGRATE_MODULE,
	NO_DISTINCT_USER_NAME,
	INVALID_OTP_TOKEN_FILE,
	NO_OTP_MODULE_INSTALLED,
	USER_HAS_NO_OTP_TOKENS,
	INVALID_AUTH_METHOD,
	AUTH_METHOD_NOT_ALLOWED,
	NO_AUTH_METHOD_FOUND,
	UNKNOWN_POLICY_APPLICATION,
	INVALID_IP_RANGE,
	INVALID_IP_FORMAT,
	SEND_VOICE_EXCEPTION,
	LDAP_INITIALIZATION_FAILED,
	DOMAIN_DISABLED,
	INVALID_HMAC_RECORD,
	USER_HAS_NO_TELEPHONE_OR_MOBILE,
	DOMAIN_INVALID_NAME,
	ARCHIVE,
	EMAIL_SEND_MSG_LIMIT,
	DB_ENCRYPTION,
	DB_DECRYTION_ERROR,
	INVALID_AUTH_SESSION_COOKIE,
	USER_MAPPED_TO_OTHER_OPERATOR,
	OPERATOR_IS_DISABLED,
	INVALID_TENANT,
	CLOUDDATA_PRIVATE,
	USER_HAS_INVALID_EMAIL,
	INVALID_DEVICE_ID,
	INVALID_ACTIVATION_CODE_ID,
	TOKEN_BELONGS_TO_SOMEONE_ELSE,
	TOKEN_ALREADY_ASSIGNED,
	INVALID_DOMAIN_NAME,
	NO_FIDO_RP_SET,
	CANNOT_CREATE_FIDO_REG_REQUEST,
	FIDO_REG_REQUEST_NOT_FOUND,
	CANNOT_PARSE_FIDO_REG_RESPONSE,
	CANNOT_VALIDATE_FIDO_REG_RESPONSE,
	CANNOT_CREATE_FIDO_AUTH_REQUEST,
	FIDO_AUTH_REQUEST_NOT_FOUND,
	CANNOT_PARSE_FIDO_AUTH_RESPONSE,
	CANNOT_VALIDATE_FIDO_AUTH_RESPONSE,
	INVALID_FIDO_AUTHENTICATOR_ID,
	INVALID_FIDO_SERVER_ID,
	INVALID_FIDO_ALLOWED_ORIGINS,
	INVALID_OTP_SERIAL_NO,
	CLOUDDATA_OUT_OF_DATE,
	URT_TOKEN_OUT_OF_DATE,
	URL_TOKEN_INVALID,
	INVALID_CLOUDDATA_ID,
	INVALID_OAUTH_CLIENT_METADATA,
	INVALID_PARAMETER,
	NO_WRITE_ACCESS,
	INVALID_CLOUDDATA_SHARE_GROUP_OR_USER,
	PORT_IN_USE,
	NODE_IS_OFF,
	NODE_IS_UNREACHABLE,
	NODE_FAILED,
	STOPPING_MODULE,
	CANNOT_CREATE_QRCODE,
	DOMAIN_CONNECTION_FAILED,
	NODE_JOINED,
	CANNOT_EDIT_OUTRANKING_USER,
	NO_MANAGEMENT_RIGHTS,
	CANNOT_EN_DE_CRYPT_PASS,
	PASSWORD_MISSING,
	LOCAL_USER_REGISTRATION_NOT_ALLOWED,
	DOMAIN_USER_REGISTRATION_NOT_ALLOWED,
	AZURE_DOMAIN_NOT_AUTHORISED,
	AZURE_DOMAIN_AUTHENTICATION_ERROR,
	AZURE_UNEXPECTED_ERROR,
	REGISTRATION_USER_ALREADY_EXISTS,
	READ_POLICY_ERROR,
	USER_DB_MANIPULATION,
	CLOUD_SAFE_FILE_TOO_BIG,
	CLOUD_SAFE_FILE_DECRYPTION,
	MEMBER_EXISTS_ALREADY,
	CLOUD_SAFE_USER_LIMIT_REACHED,
	CLOUD_SAFE_GLOBAL_LIMIT_REACHED,
	PASSWORD_SAFE_LIMIT_REACHED,
	CLOUD_SAFE_LIMIT_EXCEEDS_GLOBAL,
	CLOUD_SAFE_USER_EXPIRY_DATE_REACHED,
	CANNOT_DELETE_YOURSELF,
	PASSWORD_SAFE_NOT_ENABLED,
	LICENCE_MAX_GLOBAL_USERS_REACHED,
	PASSWORD_NOT_SUPPORTED_KDBX,
	TENANT_ALREADY_EXIST,
	UNALLOWED_ACTION,
	UNALLOWED_PATH,
	INVALID_FILE_SAFE_OWNER,
	USERID_WITH_DOMAIN_SEPARATER,
	INVALID_USERID_DOMAIN,
	NO_ITEM_SELECTED,
	OTP_DEACTIVATED,
	AUTH_SESSION_COOKIE_NOT_ALLOWED,
	FLUSH_CACHE_TO_DB,
	AUTH_PROXY_OPEN,
	AUTH_PROXY_DATA,
	CREATE_ACCOUNT_INVALID_CREDENTIALS,
	INVALID_URL_FORMAT,
	USER_MUST_RESET_PASSWORD,
	USER_ACCOUNT_LOCKED,
	USER_PASSWORD_EXPIRED,
	ALERT_NOTIFICATION_GROUP_NOT_FOUND,
	CLOUD_SAFE_NOT_SELECT_FOLDER,
	CLOUD_SAFE_WRITE_ERROR,
	REQUEST_TIMED_OUT,
	DATABASE_CONNECTION_ERROR,
	INSUFFICIENT_ACCESS_RIGHTS,
	INVALID_DATE_FORMAT,
	INVALID_ACTION_MOVE,
	CLOUD_SAFE_MOVE_FILE,
	CLOUD_SAFE_DUPLICAE_ENTRY,
	CLOUD_SAFE_DUPLICATE_NAME,
	CLOUD_SAFE_RENAME_FAILED,
	INVALID_CLOUD_SAFE_USER,
	INVALID_CLOUD_SAFE_UNIQUE_KEY,
	PASSCODE_EMPTY,
	CLOUD_SAFE_READ_ERROR,
	INVALID_CLOUDSTORAGE_TYPE,
	CLOUD_SAFE_CANNOT_RENAME_SHARED_FILE,	
	CLOUD_SAFE_CANNOT_DELETE_SHARED_FILE,
	REST_API_RESPONSE_CODE_ERROR,
	REST_API_RESPONSE_NOT_FOUND,
	INVALID_PREFERENCES_FORMAT,
	INVALID_CLOUDDATA_GROUP,
	APP_NAME_EXISTS,
	FILE_NAME_WITH_SPECIAL_CHARACTERS,
	INVALID_LICENCE_KEY_VERSION,
	UNSUPPORTED_ENCODING,
	IMAGE_SIZE_INCORRENT,
	IMAGE_TOO_BIG,
	LDAP_FAILED_READ_ATTRIBUTE,
	INVALID_INPUT_USER_FIELD,
	GENERATE_OTP_FAILED,
	NO_OTP_KEY_FOUND,
	WINDOWS_SSO_NOT_IN_DOMAIN,
	AZURE_NEEDS_MFA,
	MSAL_INVALID_STATE,
	MSAL_FAILED_TO_VALIDATE_MESSAGE,
	MSAL_LOGIN_FAILED,
	MSAL_AUTH_NO_RESULT,
	NOT_IMPLEMENTED,
	MUST_USE_AZURE_DIRECT_LOGIN,
	MISSING_META_DATA_ATRIBUTES,
	OBJECT_NOT_FOUND, 
	LICENCE_NOT_AVAILABLE, 
	OPTIMISTIC_LOCK;

	DcemErrorCodes() {
	}

}