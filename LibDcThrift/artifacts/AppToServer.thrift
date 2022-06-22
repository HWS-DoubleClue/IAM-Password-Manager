# TITLE
# 
#
# This file aims to teach you how to use Thrift, in a .thrift file. Neato. The
# first thing to notice is that .thrift files support standard shell comments.
# This lets you make your thrift file executable and include your Thrift build
# step on the top line. And you can place comments like this anywhere you like.
#
# Before running this file, you will need to have installed the thrift compiler
# into /usr/local/bin.

/**
 * The first thing to know about are types. The available types in Thrift are:
 *
 *  bool        Boolean, one byte
 *  i8        Signed byte
 *  i16         Signed 16-bit integer
 *  i32         Signed 32-bit integer
 *  i64         Signed 64-bit integer
 *  double      64-bit floating point value
 *  string      String
 *  binary      Blob (byte array)
 *  map<t1,t2>  Map from one type to another
 *  list<t1>    Ordered list of one type
 *  set<t1>     Set of unique elements of one type
 *
 * Did you also notice that Thrift supports C style comments?
 */

// Just in case you were wondering... yes. We support simple C comments too.

/**
 * Thrift files can reference other Thrift files to include common struct
 * and service definitions. These are found using the current path, or by
 * searching relative to any paths specified with the -I compiler flag.
 *
 * Included objects are accessed using the name of the .thrift file as a
 * prefix. i.e. shared.SharedObject
 */
/**
 * Thrift files can namespace, package, or prefix their output in various
 * target languages.
 */
namespace cpp shared
namespace d share // "shared" would collide with the eponymous D keyword.
namespace java com.doubleclue.comm.thrift
namespace perl shared
namespace php shared


include "AppSystem.thrift"

/**
 * Thrift lets you do typedefs to get pretty names for your types. Standard
 * C style here.
 */
// typedef i32 MyInteger

/**
 * Thrift also lets you define constants for use across languages. Complex
 * types and structs are specified using JSON notation.
 */
//const i32 INT32CONSTANT = 9853
//const map<string,string> MAPCONSTANT = {'hello':'world', 'goodnight':'moon'}

struct ServerSignatureParam {
  1: 	binary 	dataForSignature,
  2: 	AppSystem.AppVersion 	appVersion,
  3:	AppSystem.AppVersion	libVersion,
  4:    string  domainName,
  5:    bool    iamDcem,   		// Deprecated
  6:    AppSystem.CommClientType commClientType,
}

struct ServerSignatureResponse {
  1: 	binary 	serverSignature,
  2: 	AppSystem.AppVersion 	serverVersion,
  3:    bool isReverseProxy,
  4:    bool isDispatched, 
  5:    optional binary challenge     // since 1.5 Only for AuthApp Clients 
}

struct DomainSdkConfigParam {
  1:    string  domainName,
  2: 	string 	activationCode,
  3: 	string 	userId,
  4:    optional binary dispatcherSignature;
}

struct DomainSdkConfigResponse {
  1: 	optional binary	sdkConfigDcem;
}

struct RegisterDispatcherParam {
  1:    string  domainName,
  2: 	binary 	dispatcherSignature,   	// DCEM have to verify signature
  3: 	string 	pnKey;
}

struct RegisterDispatcherResponse {
  1:    string  clusterId,
}


struct ActivationParam {
  1: 	string 	activationCode,
  2: 	string 	userId,
  3: 	binary 	udid,
  4: 	binary 	encPassword,
  5: 	AppSystem.CpuArch cpuArch,
  6: 	string 	osVersion,
  7: 	string 	locale,			// 2 ISO digits 
  8:	string	manufacture,
  9:	string	deviceModel,
  10:   string deviceName,
  11:   binary  publicKey,
  12:   binary  signature,
  13:   AppSystem.ClientType clientType,
  14:   optional binary digest,
  15:   optional binary risk,
}

struct ActivationResponse {
  1: 	bool 	updateAvailable,
  2: 	i32 	deviceId,
  3:    binary  signedCertificate, 
  4: 	binary 	deviceKey,
  5:    binary  offlineKey,
  6:    string deviceName
}

struct LoginParam {
  1: 	i32 	deviceId,
  2: 	binary 	udid,
  3: 	binary 	encPassword,
  4: 	string 	osVersion,
  5: 	string 	locale,				// 2 digits
  6:	i32		hotpCounter,    	// not implemented yet
  7:    optional binary digest,	// not implemented yet
  8:    optional binary risk,		// not implemented yet
  9:    bool    passwordLess  		// If true, the password is an offline-passcode
  10:   AppSystem.CpuArch cpuArch,
  11:   AppSystem.CommClientType commClientType,
  12:   optional string sessionCookie,
  
}

struct LoginResponse {
  1: 	binary 	deviceKey,				//  null if password-less login
  2:    string  oneTimePassword,  		// 32 characters, null if password-less login
  3:	i64 	updateAvailableTill,  // 
  4:    i32     passcodeValidFor,     // passcode valid for minutes
  5:    optional string  updateInfo,
  6:	i64 	licenceExpiresOn,  // 
  7: 	bool	testLicence,
  8:    bool    ldapUser,
  9:  	i32 	keepAliveSeconds
  10:   optional string sessionCookie,
  11:   optional i32 sessionCookieExpiresOn,
}

struct RequestActivationCodeResponse {
  1:    string  activationCode, 
  2:	i64 	validTill,  // 
}

struct AuthUserParam {
  1:	string  authGatewayId,
  2:    string  loginId, 
  3:    AppSystem.ThriftAuthMethod   authMethod,   // 
  4:	binary 	encPassword,  	//
  5:    binary  encPasscode,
  6:    binary	sharedSecret,   // encryption of the ServerSignatureResponse Challenge
  7: 	binary 	udid, 
  8:    string  workstationName,
  9:    AppSystem.ClientType clientType
  10:   map<string, string> propertyMap
}

struct AuthUserResponse {
  1:  bool  success,
  2:  optional list<AppSystem.ThriftAuthMethod> authMethods,	
  3:  i32	responseTime, 
  4:  i64   msgId,
  5:  optional binary  userKey,
  6:  optional string sessionCookie,
  7:  optional i32 sessionCookieExpiresOn,
  8:  optional string secureMsgRandomCode,
  9:  optional string phoneNumber,
  10: optional string fqUserLoginId,
  11: optional string fidoResponse,
  12: optional string ldapDomain,
}

struct AuthConnectParam {
  1:	string  authGatewayId,
  2:    binary	sharedSecret,   // encryption of the ServerSignatureResponse Challenge
  3:    string  workstationName,
  4: 	binary 	udid, 
}

struct AuthSelectParam {
  1:	string  authGatewayId,
  2:    string  loginId, 
  3:    binary	sharedSecret,   // encryption of the ServerSignatureResponse Challenge
  4: 	binary 	udid, 
  5:    string  workstationName,
  6:    AppSystem.ClientType clientType
}


struct AuthSelectResponse {
  1:	list<AppSystem.ThriftAuthMethod> authMethods;
}

struct QrCodeResponse {
  1:	i32	timeToLive,
  2:	string	data
}


struct SignatureParam {
/*
* This is a PKCS#1.5 (or better to use PKCS#1 version 2.0 signature, using the device private, of the loginOtp received in LoginResponse
*/
  1: 	binary 	clientSignature,      
  2: 	binary 	appDigest,
}

struct SignatureResponse {

  1:    binary  reconnectTicket,
  2:	i32 keepAliveSeconds
       
}


/*
struct ReconnectParam {
  1: 	i32 	deviceId,
  2: 	binary 	reconnectTicket,
  3: 	i32 	lastRequestSend,
  4: 	i32 	lastRequestReceived
}
*/


struct DeviceOfflineKey {
  1:	optional binary	udid,
  2:	binary	offlineKey,
  3:	i32		window,
  4:	i32		validFor,
  5:	string	algorithm
}


/**
 * Ahh, now onto the cool part, defining a service. Services just need a name
 * and can optionally inherit from another service using the extends keyword.
 */
service AppToServer {

   /**
   * A method definition looks like C code. It has a return type, arguments,
   * and optionally a list of exceptions that it may throw. Note that argument
   * lists and exception lists are specified using the exact same syntax as
   * field lists in struct or exception definitions.
   */
   
   ServerSignatureResponse serverSignature (1:ServerSignatureParam serverSignatureParam) throws (1:AppSystem.AppException ouch),
   
   DomainSdkConfigResponse getDomainSdkConfig (1:DomainSdkConfigParam domainSdkConfigParam) throws (1:AppSystem.AppException ouch),
   
   RegisterDispatcherResponse registerDispatcher (1:RegisterDispatcherParam registerDispatcherParam) throws (1:AppSystem.AppException ouch),
   
   
   ActivationResponse activation (1:ActivationParam activationParam) throws (1:AppSystem.AppException ouch),
   
   LoginResponse login (1:LoginParam loginParam) throws (1:AppSystem.AppException ouch),
   
   SignatureResponse clientSignature (1:SignatureParam signaturenParam) throws (1:AppSystem.AppException ouch),

	AppSystem.Template getTemplateFromId (1: i32 id) throws (1:AppSystem.AppException ouch),
	
	bool sendMessage (1:AppSystem.AppMessage appMessage) throws (1:AppSystem.AppException ouch),
	
	void sendMessageResponse (1:AppSystem.AppMessageResponse appMessageResponse) throws (1:AppSystem.AppException ouch),
	
	void sendLoginQrCode (1:string data) throws (1:AppSystem.AppException ouch),
	
	QrCodeResponse getLoginQrCode (1:string operatorId, 2:string sessionId) throws (1:AppSystem.AppException ouch),
	
	void changePassword (1:binary encPassword, 2:binary newEncPassword) throws (1:AppSystem.AppException ouch),
	
	AppSystem.SdkCloudSafe getCloudSafe (1:AppSystem.SdkCloudSafeKey uniqueKey, 2:string userLoginId) throws (1:AppSystem.AppException ouch),
	
	void renameCloudSafe (1:AppSystem.SdkCloudSafeKey uniqueKey, 2:string userLoginId, 3:string newName) throws (1:AppSystem.AppException ouch),
	
	void deleteCloudSafe (1:AppSystem.SdkCloudSafeKey uniqueKey, 2:string userLoginId) throws (1:AppSystem.AppException ouch),
		
	i64 setCloudSafe (1:AppSystem.SdkCloudSafe sdkCloudSafe) throws (1:AppSystem.AppException ouch),
	
	list<AppSystem.SdkCloudSafe> getCloudSafeList (1:string nameFilter, 2: bool includeShare 3:i64 modifiedFromEpoch, 4:AppSystem.CloudSafeOwner owner) throws (1:AppSystem.AppException ouch)
		   
    void disconnect (1: AppSystem.AppErrorCodes appErrorCodes, 2: string message),
    
    void keepAlive();
    
    void deactivate() throws (1:AppSystem.AppException ouch); 
    
    RequestActivationCodeResponse requestActivationCode() throws (1:AppSystem.AppException ouch);
    
    void verifyPassword(1:binary encPassword) throws (1:AppSystem.AppException ouch);
    
    AuthUserResponse authenticateUser (1:AuthUserParam authUserParam) throws (1:AppSystem.AppException ouch);
    
    AuthSelectResponse getAuthenticationMethods (1:AuthSelectParam authSelectParam) throws (1:AppSystem.AppException ouch);
    
    list<DeviceOfflineKey> getDeviceOfflineKeys() throws (1:AppSystem.AppException ouch);
    
    i32 authConnect (1:AuthConnectParam authUserParam) throws (1:AppSystem.AppException ouch);
    
    binary proxyData (1: i64 handle, 2:binary data ) throws (1:AppSystem.AppException ouch);
   
   	void proxyClose (1: i64 handle);
    

}

/**
 * That just about covers the basics. Take a look in the test/ folder for more
 * detailed examples. After you run this file, your generated code shows up
 * in folders with names gen-<language>. The generated code isn't too scary
 * to look at. It even has pretty indentation.
 */
