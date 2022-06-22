/**
 * This Thrift file can be included by other Thrift files that want to share
 * these definitions.
 */

namespace cpp shared
namespace d share // "shared" would collide with the eponymous D keyword.
namespace java com.doubleclue.comm.thrift
namespace perl shared
namespace php shared

const i8 PROTOCOL_VERSION = 0x31
const i8 PROTOCOL_APP_TO_SERVER = 0x41
const i8 PROTOCOL_SERVER_TO_APP = 0x53
const i8 PROTOCOL_REVERSE_PROXY = 0x42
const i8 REVERSE_PROXY_OPEN = 1;
const i8 REVERSE_PROXY_CLOSE = 2;
const i8 REVERSE_PROXY_DATA = 3;

const string SIGNATURE_STRING = "This is the SEM-DoubleCheck signature. This is user to verify the signatures"

const string DcemFileName = "SdkConfig.dcem"
const string SdkConfigFileName = "SdkConfig.as"
const string SignatureFileName = "SdkConfig.sig"
const string TrustStoreFileName = "TrustStore.pem"
const string AuthConnectorFileName = "AuthConnector.dcem"

const string UserFullQualifiedId = "userFqID"
const string PasswordLessLogin = "passwordLessLogin"
const string PushNotificationTitle = "pntitle"
const string PushNotificationBody = "pnBody"
const string PushNotificationAction = "pnAction"



const string TENANT_SEPERATOR = "!"
const string REALM_SEPERATOR = "$"

const string PUSH_NOTIFICATION_TOKEN = "hws.pushnotification.token"

const string AUTH_PARAM_UNLOCK = "unlock"
const string AUTH_PARAM_USE_ALTERNATIVES = "useAlternatives"
const string TRUE_VALUE = "true"

exception AppException {
  1: string error,
  2: optional string info
}

enum CloudSafeOptions {
	ENC,
	PWD,
	FPD;	
}

enum AppErrorCodes { 
  OK = 0,   
  WRONG_CREDENTIALS = 1,
  INVALID_ACTIVATION_CODE = 2,
  ACTIVATION_CODE_EXPIRED = 3,
  USER_PASSWORD_MAX_RETRIES = 4, 
  UNKNOWN_CLIENT_TYPE = 5,
  UNKNOWN_ARCH_TYPE = 6,
  REJECT_RISK = 7,
  UPDATE_NECESSARY = 8,
  INVALID_USERID = 9,
  UNEXPECTED_ERROR = 10,
  INVALID_VERSION = 11,
  INVALID_PIN = 12,
  CSR_SIGNATURE = 13,
  CSR_ERROR = 14
  TRANSPORT_ERROR = 15
  USER_DISABLED = 16
  USER_TEMPORARY_DISABLED = 17
  INVALID_DEVICE_NAME = 18
  INVALID_DEVICE_ID = 19
  DEVICE_DISABLED = 20
  INCORRECT_STATE = 21
  INVALID_UDID = 22
  INVALID_CLIENT_SIGNATURE = 23
  NO_TEMPLATE_FOUND = 24
  RESPONSE_MESSAGE_ERROR = 25
  CLIENT_ERROR = 26
  RESPONSE_MESSAGE_INVALID_ID = 27
  INVALID_PASSWORD = 28
  DUPLICATED_DEVICE_NAME = 29
  REST_ADD_MESSAGE_FAILURE = 30
  APP_MSG_RESPONSE_TIMEDOUT = 31
  INVALID_MESSAGE_SIGNATURE = 32
  PROPERTY_NOT_FOUND = 33
  VERSION_UPDATED_REQUIRED = 34
  VERSION_DISABLED = 35
  FCM_PUSHNOTIFICATION = 36
  INVALID_CLOUD_SAFE_SIGNATURE = 37
  CLOUD_SAFE_NOT_FOUND = 38 
  LICENCE_EXPIRED = 39 
  LICENCE_MAX_USER = 40 
  INVALID_DOMAIN_NAME = 41
  DISPATCHER_CONNECTION_TO_DCEM_FAILED = 42;
  CONNECTION_TO_LDAP_FAILED = 43;
  UNREGISTERED_DCEM = 44;
  NO_CONNECTION_TO_DESTINATION_DCEM = 45;
  NOT_A_DISPATCHER_PROXY = 46;
  DOMAIN_CANNOT_CONNECT_TO_ITSELF = 47;
  DOMAIN_HAS_NO_CONFIGURATION = 48;
  REVERSE_PROXY_CONNECTION_LIMIT = 49;
  INVALID_DEVICE_KEY = 50;
  INVALID_OFFLINE_KEY = 51;
  USER_HAS_NO_DEVICES = 52;
  NO_DISTINCT_USER_NAME = 53;
  INVALID_PASSCODE = 54;  
  PASSCODE_NOT_NUMERIC = 55;
  INVALID_AUTH_METHOD = 56;
  AUTH_METHOD_NOT_ALLOWED = 57;
  NO_AUTH_METHOD_FOUND = 58;
  SMS_USER_HAS_NO_MOBILE = 59;
  SMS_SEND_EXCEPTION = 60;
  SEND_VOICE_EXCEPTION = 61;
  INVALID_OTP = 62;
  USER_HAS_NO_OTP_TOKENS = 63;
  INVALID_TENANT_IDENTIFIER = 64;
  USER_HAS_NO_TELEPHONE_OR_MOBILE = 65;
  INVALID_CLOUD_SAFE_OWNER = 66;
  APP_MSG_IGNORED = 67;
  DB_DECRYTION_ERROR = 68;
  NO_WRITE_ACCESS = 69;
  REVERSE_PROXY_REDIRECTION = 70;
  PASSWORD_SAFE_LIMIT_REACHED = 71;
  CLOUD_SAFE_LIMIT_REACHED = 72;
  PASSWORD_SAFE_NOT_ENABLED = 73;
  CLOUD_SAFE_GLOBAL_LIMIT_REACHED = 74;
  CLOUD_SAFE_USER_LIMIT_REACHED = 75;
  CLOUD_SAFE_USER_EXPIRY_DATE_REACHED = 76;
  CLOUD_SAFE_FILE_DECRYPTION = 77; 
  LICENCE_MAX_USERS_REACHED = 78; 
  LICENCE_MAX_GLOBAL_USERS_REACHED = 79;
  INVALID_FILE_SAFE_OWNER = 80;
  CLOUDDATA_OUT_OF_DATE = 81;
  CREATE_ACCOUNT_INVALID_CREDENTIALS = 82;
  INVALID_AUTH_SESSION_COOKIE = 83;
  UNSUPPORTED_METHOD = 84;
  ID_WITH_SPECIAL_CHARACTERS = 85;
  CLOUD_SAFE_CANNOT_RENAME_SHARED_FILE = 86;
  CLOUD_SAFE_CANNOT_DELETE_SHARED_FILE = 87
}

enum CommClientType {
	APP = 1,
	DCEM_AS_CLIENT = 2,
	AUTH_APP = 3;
} 

enum MsgPriority {    
  IMMEDIATE = 1,
  URGENT = 2,
  HIGH = 3,
  NORAML = 4,
  LOW = 5,
  NEGLIGIBLE = 6
  NONE = 7
}

enum ThriftAuthMethod {
	AUTO = -1,
	PASSWORD = 0,
	SMS = 1,
	VOICE_MESSAGE = 2,
	HARDWARE_TOKEN = 3,
	DOUBLECLUE_PASSCODE = 4, 
	PUSH_APPROVAL = 5,
	QRCODE_APPROVAL = 6,
	SESSION_RECONNECT = 7,
	FIDO_U2F = 8;
}

struct SdkConfig {
	1: binary connectionKey;
	2: string serverUrl;
	3: string transportProtocol;
	4: binary serverPublicKey;
	5: optional string portalUrl;	 
}

struct AuthGatewayConfig {
	1: string name;
	2: binary sharedSecret;
	3: string tenantName;
}

struct SdkSettings {
	1: i32 deviceId,
    2: string userId,
    3: string deviceName,
	4: binary privateKey,
	5: binary publicKey,
	6: binary offlineKey,
	7: i32 passcodeValidFor,
	8: map <string, string> safe,
	9: binary sessionCookie
	10: i32 sessionCookieExpiresOn
}

struct MsgAttachment {
	1:  i64  id,
	2:	string name,
	3:	string mimeType,
	4:	i32	size;
}


struct AppMessage {
  	1:	i64 	id,
  	2:  MsgPriority priority,
  	3:  i32     templateId,
  	4:  bool	signitureRequired,   
	5:	bool	responseRequired, 
	6:  i32		responseTime,
	7:  i64     responseTo,
	8:	map <string, string>  data,
//	9: 	optional list<MsgAttachment> attachements		
}

struct AppMessageResponse {
  	1:	i64 	id,
  	2:  string	actionId;
  	3:  AppErrorCodes errorCode;
  	4:  string  errorMessage;
  	5: 	bool	read,
 	6:	map <string, string>  responseData,
 	7:	optional binary signature,	
//	5: 	optional list<MsgAttachment> attachements		
}

struct AuthAppMessageResponse {
  	1:	bool succesful,
  	2:  optional string errorCode,
  	3:  optional string errorMessage,
  	4:  optional binary  userKey,
  	5:  optional i32 sessionCookieExpiresOn,  
  	6:  optional string sessionCookie,  
  	7:  i64  msgId		
}



enum ClientType {
  ANDROID = 0,
  I_OS = 1,
  WINDOWS = 2,
  MAC = 3,
  Linux = 4;
}

enum CpuArch {
  unknown = 0,	
  x86 = 1,
  asm = 2,

}

enum CloudSafeOwner {
  GLOBAL = 0,
  USER = 1,
  DEVICE = 2,
  GROUP = 3
}


struct AppVersion {
  1: i32 version,
  2: string name,
  3: string state
}

struct Template {
  1: 	i32 		id,
  2:    string  	name,
  3:    string  	locale,
  4:   	optional string  content        
}

struct SdkCloudSafeKey {
  1: CloudSafeOwner owner,
  2: string name,
  3: optional i64  dbId,
  4: optional string groupName  // this is option in case owner is group
}

struct SdkCloudSafe {
  1:    SdkCloudSafeKey uniqueKey,
  2: 	binary 		content,
  3: 	string 		options,
  4:	i64			discardAfter,
  5:    i64			lastModified,
  6:	binary		signature,
  7:    i64         length,
  8:    string		sharedUser,
  9:    bool		writeAccess,
  10:   bool		restictDownload
}

struct User {
	1: string loginId,
	2: bool updatePushNotification,
	3: bool usingReverseProxy
}


struct ActivatedUsers {
	1: list <User> users
}




struct RpOpen {
  1: string remoteAddress,
  2: AppVersion appVersion,
  3: AppVersion libVersion,
  4: string tenantName
}

struct AppAccount {
	1: string name,
	2: string userName,
	3: string tenantName,
	4: string realmName,
	5: string fullQualifiedName,
	6: bool updatePushNotification,
	7: bool usingReverseProxy,
	8: string settingsFileName
}

struct AppAccounts {
	1: list<AppAccount> accounts
}

struct ProxyOpenParam {
	1: i64 handle, 
	2: string ipHost, 
	3: i32 port, 
	4: bool secure, 
	5: bool verifyCertificate,
	6: i32 dataWait   // wait in milliseconds. If zero return immediatly else wait for receive data for this time. 

}




service AppSystem {
 

}


