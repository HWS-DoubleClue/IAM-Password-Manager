/**
 * Autogenerated by Thrift Compiler (0.13.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */

import Foundation

import Thrift


/// Thrift also lets you define constants for use across languages. Complex
/// types and structs are specified using JSON notation.
public final class ServerSignatureParam {

  public var dataForSignature: Data

  public var appVersion: AppVersion

  public var libVersion: AppVersion

  public var domainName: String

  public var iamDcem: Bool

  public var commClientType: CommClientType


  public init(dataForSignature: Data, appVersion: AppVersion, libVersion: AppVersion, domainName: String, iamDcem: Bool, commClientType: CommClientType) {
    self.dataForSignature = dataForSignature
    self.appVersion = appVersion
    self.libVersion = libVersion
    self.domainName = domainName
    self.iamDcem = iamDcem
    self.commClientType = commClientType
  }

}

public final class ServerSignatureResponse {

  public var serverSignature: Data

  public var serverVersion: AppVersion

  public var isReverseProxy: Bool

  public var isDispatched: Bool

  public var challenge: Data?


  public init(serverSignature: Data, serverVersion: AppVersion, isReverseProxy: Bool, isDispatched: Bool) {
    self.serverSignature = serverSignature
    self.serverVersion = serverVersion
    self.isReverseProxy = isReverseProxy
    self.isDispatched = isDispatched
  }

  public init(serverSignature: Data, serverVersion: AppVersion, isReverseProxy: Bool, isDispatched: Bool, challenge: Data?) {
    self.serverSignature = serverSignature
    self.serverVersion = serverVersion
    self.isReverseProxy = isReverseProxy
    self.isDispatched = isDispatched
    self.challenge = challenge
  }

}

public final class DomainSdkConfigParam {

  public var domainName: String

  public var activationCode: String

  public var userId: String

  public var dispatcherSignature: Data?


  public init(domainName: String, activationCode: String, userId: String) {
    self.domainName = domainName
    self.activationCode = activationCode
    self.userId = userId
  }

  public init(domainName: String, activationCode: String, userId: String, dispatcherSignature: Data?) {
    self.domainName = domainName
    self.activationCode = activationCode
    self.userId = userId
    self.dispatcherSignature = dispatcherSignature
  }

}

public final class DomainSdkConfigResponse {

  public var sdkConfigDcem: Data?


  public init() { }
  public init(sdkConfigDcem: Data?) {
    self.sdkConfigDcem = sdkConfigDcem
  }

}

public final class RegisterDispatcherParam {

  public var domainName: String

  public var dispatcherSignature: Data

  public var pnKey: String


  public init(domainName: String, dispatcherSignature: Data, pnKey: String) {
    self.domainName = domainName
    self.dispatcherSignature = dispatcherSignature
    self.pnKey = pnKey
  }

}

public final class RegisterDispatcherResponse {

  public var clusterId: String


  public init(clusterId: String) {
    self.clusterId = clusterId
  }

}

public final class ActivationParam {

  public var activationCode: String

  public var userId: String

  public var udid: Data

  public var encPassword: Data

  public var cpuArch: CpuArch

  public var osVersion: String

  public var locale: String

  public var manufacture: String

  public var deviceModel: String

  public var deviceName: String

  public var publicKey: Data

  public var signature: Data

  public var clientType: ClientType

  public var digest: Data?

  public var risk: Data?


  public init(activationCode: String, userId: String, udid: Data, encPassword: Data, cpuArch: CpuArch, osVersion: String, locale: String, manufacture: String, deviceModel: String, deviceName: String, publicKey: Data, signature: Data, clientType: ClientType) {
    self.activationCode = activationCode
    self.userId = userId
    self.udid = udid
    self.encPassword = encPassword
    self.cpuArch = cpuArch
    self.osVersion = osVersion
    self.locale = locale
    self.manufacture = manufacture
    self.deviceModel = deviceModel
    self.deviceName = deviceName
    self.publicKey = publicKey
    self.signature = signature
    self.clientType = clientType
  }

  public init(activationCode: String, userId: String, udid: Data, encPassword: Data, cpuArch: CpuArch, osVersion: String, locale: String, manufacture: String, deviceModel: String, deviceName: String, publicKey: Data, signature: Data, clientType: ClientType, digest: Data?, risk: Data?) {
    self.activationCode = activationCode
    self.userId = userId
    self.udid = udid
    self.encPassword = encPassword
    self.cpuArch = cpuArch
    self.osVersion = osVersion
    self.locale = locale
    self.manufacture = manufacture
    self.deviceModel = deviceModel
    self.deviceName = deviceName
    self.publicKey = publicKey
    self.signature = signature
    self.clientType = clientType
    self.digest = digest
    self.risk = risk
  }

}

public final class ActivationResponse {

  public var updateAvailable: Bool

  public var deviceId: Int32

  public var signedCertificate: Data

  public var deviceKey: Data

  public var offlineKey: Data

  public var deviceName: String


  public init(updateAvailable: Bool, deviceId: Int32, signedCertificate: Data, deviceKey: Data, offlineKey: Data, deviceName: String) {
    self.updateAvailable = updateAvailable
    self.deviceId = deviceId
    self.signedCertificate = signedCertificate
    self.deviceKey = deviceKey
    self.offlineKey = offlineKey
    self.deviceName = deviceName
  }

}

public final class LoginParam {

  public var deviceId: Int32

  public var udid: Data

  public var encPassword: Data

  public var osVersion: String

  public var locale: String

  public var hotpCounter: Int32

  public var digest: Data?

  public var risk: Data?

  public var passwordLess: Bool

  public var cpuArch: CpuArch

  public var commClientType: CommClientType

  public var sessionCookie: String?


  public init(deviceId: Int32, udid: Data, encPassword: Data, osVersion: String, locale: String, hotpCounter: Int32, passwordLess: Bool, cpuArch: CpuArch, commClientType: CommClientType) {
    self.deviceId = deviceId
    self.udid = udid
    self.encPassword = encPassword
    self.osVersion = osVersion
    self.locale = locale
    self.hotpCounter = hotpCounter
    self.passwordLess = passwordLess
    self.cpuArch = cpuArch
    self.commClientType = commClientType
  }

  public init(deviceId: Int32, udid: Data, encPassword: Data, osVersion: String, locale: String, hotpCounter: Int32, digest: Data?, risk: Data?, passwordLess: Bool, cpuArch: CpuArch, commClientType: CommClientType, sessionCookie: String?) {
    self.deviceId = deviceId
    self.udid = udid
    self.encPassword = encPassword
    self.osVersion = osVersion
    self.locale = locale
    self.hotpCounter = hotpCounter
    self.digest = digest
    self.risk = risk
    self.passwordLess = passwordLess
    self.cpuArch = cpuArch
    self.commClientType = commClientType
    self.sessionCookie = sessionCookie
  }

}

public final class LoginResponse {

  public var deviceKey: Data

  public var oneTimePassword: String

  public var updateAvailableTill: Int64

  public var passcodeValidFor: Int32

  public var updateInfo: String?

  public var licenceExpiresOn: Int64

  public var testLicence: Bool

  public var ldapUser: Bool

  public var keepAliveSeconds: Int32

  public var sessionCookie: String?

  public var sessionCookieExpiresOn: Int32?


  public init(deviceKey: Data, oneTimePassword: String, updateAvailableTill: Int64, passcodeValidFor: Int32, licenceExpiresOn: Int64, testLicence: Bool, ldapUser: Bool, keepAliveSeconds: Int32) {
    self.deviceKey = deviceKey
    self.oneTimePassword = oneTimePassword
    self.updateAvailableTill = updateAvailableTill
    self.passcodeValidFor = passcodeValidFor
    self.licenceExpiresOn = licenceExpiresOn
    self.testLicence = testLicence
    self.ldapUser = ldapUser
    self.keepAliveSeconds = keepAliveSeconds
  }

  public init(deviceKey: Data, oneTimePassword: String, updateAvailableTill: Int64, passcodeValidFor: Int32, updateInfo: String?, licenceExpiresOn: Int64, testLicence: Bool, ldapUser: Bool, keepAliveSeconds: Int32, sessionCookie: String?, sessionCookieExpiresOn: Int32?) {
    self.deviceKey = deviceKey
    self.oneTimePassword = oneTimePassword
    self.updateAvailableTill = updateAvailableTill
    self.passcodeValidFor = passcodeValidFor
    self.updateInfo = updateInfo
    self.licenceExpiresOn = licenceExpiresOn
    self.testLicence = testLicence
    self.ldapUser = ldapUser
    self.keepAliveSeconds = keepAliveSeconds
    self.sessionCookie = sessionCookie
    self.sessionCookieExpiresOn = sessionCookieExpiresOn
  }

}

public final class RequestActivationCodeResponse {

  public var activationCode: String

  public var validTill: Int64


  public init(activationCode: String, validTill: Int64) {
    self.activationCode = activationCode
    self.validTill = validTill
  }

}

public final class AuthUserParam {

  public var authGatewayId: String

  public var loginId: String

  public var authMethod: ThriftAuthMethod

  public var encPassword: Data

  public var encPasscode: Data

  public var sharedSecret: Data

  public var udid: Data

  public var workstationName: String

  public var clientType: ClientType

  public var propertyMap: TMap<String, String>


  public init(authGatewayId: String, loginId: String, authMethod: ThriftAuthMethod, encPassword: Data, encPasscode: Data, sharedSecret: Data, udid: Data, workstationName: String, clientType: ClientType, propertyMap: TMap<String, String>) {
    self.authGatewayId = authGatewayId
    self.loginId = loginId
    self.authMethod = authMethod
    self.encPassword = encPassword
    self.encPasscode = encPasscode
    self.sharedSecret = sharedSecret
    self.udid = udid
    self.workstationName = workstationName
    self.clientType = clientType
    self.propertyMap = propertyMap
  }

}

public final class AuthUserResponse {

  public var success: Bool

  public var authMethods: TList<ThriftAuthMethod>?

  public var responseTime: Int32

  public var msgId: Int64

  public var userKey: Data?

  public var sessionCookie: String?

  public var sessionCookieExpiresOn: Int32?

  public var secureMsgRandomCode: String?

  public var phoneNumber: String?

  public var fqUserLoginId: String?

  public var fidoResponse: String?

  public var ldapDomain: String?


  public init(success: Bool, responseTime: Int32, msgId: Int64) {
    self.success = success
    self.responseTime = responseTime
    self.msgId = msgId
  }

  public init(success: Bool, authMethods: TList<ThriftAuthMethod>?, responseTime: Int32, msgId: Int64, userKey: Data?, sessionCookie: String?, sessionCookieExpiresOn: Int32?, secureMsgRandomCode: String?, phoneNumber: String?, fqUserLoginId: String?, fidoResponse: String?, ldapDomain: String?) {
    self.success = success
    self.authMethods = authMethods
    self.responseTime = responseTime
    self.msgId = msgId
    self.userKey = userKey
    self.sessionCookie = sessionCookie
    self.sessionCookieExpiresOn = sessionCookieExpiresOn
    self.secureMsgRandomCode = secureMsgRandomCode
    self.phoneNumber = phoneNumber
    self.fqUserLoginId = fqUserLoginId
    self.fidoResponse = fidoResponse
    self.ldapDomain = ldapDomain
  }

}

public final class AuthConnectParam {

  public var authGatewayId: String

  public var sharedSecret: Data

  public var workstationName: String

  public var udid: Data


  public init(authGatewayId: String, sharedSecret: Data, workstationName: String, udid: Data) {
    self.authGatewayId = authGatewayId
    self.sharedSecret = sharedSecret
    self.workstationName = workstationName
    self.udid = udid
  }

}

public final class AuthSelectParam {

  public var authGatewayId: String

  public var loginId: String

  public var sharedSecret: Data

  public var udid: Data

  public var workstationName: String

  public var clientType: ClientType


  public init(authGatewayId: String, loginId: String, sharedSecret: Data, udid: Data, workstationName: String, clientType: ClientType) {
    self.authGatewayId = authGatewayId
    self.loginId = loginId
    self.sharedSecret = sharedSecret
    self.udid = udid
    self.workstationName = workstationName
    self.clientType = clientType
  }

}

public final class AuthSelectResponse {

  public var authMethods: TList<ThriftAuthMethod>


  public init(authMethods: TList<ThriftAuthMethod>) {
    self.authMethods = authMethods
  }

}

public final class QrCodeResponse {

  public var timeToLive: Int32

  public var data: String


  public init(timeToLive: Int32, data: String) {
    self.timeToLive = timeToLive
    self.data = data
  }

}

public final class SignatureParam {

  public var clientSignature: Data

  public var appDigest: Data


  public init(clientSignature: Data, appDigest: Data) {
    self.clientSignature = clientSignature
    self.appDigest = appDigest
  }

}

public final class SignatureResponse {

  public var reconnectTicket: Data

  public var keepAliveSeconds: Int32


  public init(reconnectTicket: Data, keepAliveSeconds: Int32) {
    self.reconnectTicket = reconnectTicket
    self.keepAliveSeconds = keepAliveSeconds
  }

}

public final class DeviceOfflineKey {

  public var udid: Data?

  public var offlineKey: Data

  public var window: Int32

  public var validFor: Int32

  public var algorithm: String


  public init(offlineKey: Data, window: Int32, validFor: Int32, algorithm: String) {
    self.offlineKey = offlineKey
    self.window = window
    self.validFor = validFor
    self.algorithm = algorithm
  }

  public init(udid: Data?, offlineKey: Data, window: Int32, validFor: Int32, algorithm: String) {
    self.udid = udid
    self.offlineKey = offlineKey
    self.window = window
    self.validFor = validFor
    self.algorithm = algorithm
  }

}

/// Ahh, now onto the cool part, defining a service. Services just need a name
/// and can optionally inherit from another service using the extends keyword.
public protocol AppToServer {

  /// A method definition looks like C code. It has a return type, arguments,
  /// and optionally a list of exceptions that it may throw. Note that argument
  /// lists and exception lists are specified using the exact same syntax as
  /// field lists in struct or exception definitions.
  ///
  /// - Parameters:
  ///   - serverSignatureParam: 
  /// - Returns: ServerSignatureResponse
  /// - Throws: AppException
  func serverSignature(serverSignatureParam: ServerSignatureParam) throws -> ServerSignatureResponse

  ///
  /// - Parameters:
  ///   - domainSdkConfigParam: 
  /// - Returns: DomainSdkConfigResponse
  /// - Throws: AppException
  func getDomainSdkConfig(domainSdkConfigParam: DomainSdkConfigParam) throws -> DomainSdkConfigResponse

  ///
  /// - Parameters:
  ///   - registerDispatcherParam: 
  /// - Returns: RegisterDispatcherResponse
  /// - Throws: AppException
  func registerDispatcher(registerDispatcherParam: RegisterDispatcherParam) throws -> RegisterDispatcherResponse

  ///
  /// - Parameters:
  ///   - activationParam: 
  /// - Returns: ActivationResponse
  /// - Throws: AppException
  func activation(activationParam: ActivationParam) throws -> ActivationResponse

  ///
  /// - Parameters:
  ///   - loginParam: 
  /// - Returns: LoginResponse
  /// - Throws: AppException
  func login(loginParam: LoginParam) throws -> LoginResponse

  ///
  /// - Parameters:
  ///   - signaturenParam: 
  /// - Returns: SignatureResponse
  /// - Throws: AppException
  func clientSignature(signaturenParam: SignatureParam) throws -> SignatureResponse

  ///
  /// - Parameters:
  ///   - id: 
  /// - Returns: Template
  /// - Throws: AppException
  func getTemplateFromId(id: Int32) throws -> Template

  ///
  /// - Parameters:
  ///   - appMessage: 
  /// - Returns: Bool
  /// - Throws: AppException
  func sendMessage(appMessage: AppMessage) throws -> Bool

  ///
  /// - Parameters:
  ///   - appMessageResponse: 
  /// - Throws: AppException
  func sendMessageResponse(appMessageResponse: AppMessageResponse) throws

  ///
  /// - Parameters:
  ///   - data: 
  /// - Throws: AppException
  func sendLoginQrCode(data: String) throws

  ///
  /// - Parameters:
  ///   - operatorId: 
  ///   - sessionId: 
  /// - Returns: QrCodeResponse
  /// - Throws: AppException
  func getLoginQrCode(operatorId: String, sessionId: String) throws -> QrCodeResponse

  ///
  /// - Parameters:
  ///   - encPassword: 
  ///   - newEncPassword: 
  /// - Throws: AppException
  func changePassword(encPassword: Data, newEncPassword: Data) throws

  ///
  /// - Parameters:
  ///   - uniqueKey: 
  ///   - userLoginId: 
  /// - Returns: SdkCloudSafe
  /// - Throws: AppException
  func getCloudSafe(uniqueKey: SdkCloudSafeKey, userLoginId: String) throws -> SdkCloudSafe

  ///
  /// - Parameters:
  ///   - sdkCloudSafe: 
  /// - Returns: Int64
  /// - Throws: AppException
  func setCloudSafe(sdkCloudSafe: SdkCloudSafe) throws -> Int64

  ///
  /// - Parameters:
  ///   - nameFilter: 
  ///   - includeShare: 
  ///   - modifiedFromEpoch: 
  ///   - owner: 
  /// - Returns: TList<SdkCloudSafe>
  /// - Throws: AppException
  func getCloudSafeList(nameFilter: String, includeShare: Bool, modifiedFromEpoch: Int64, owner: CloudSafeOwner) throws -> TList<SdkCloudSafe>

  ///
  /// - Parameters:
  ///   - appErrorCodes: 
  ///   - message: 
  /// - Throws: 
  func disconnect(appErrorCodes: AppErrorCodes, message: String) throws

  ///
  /// - Throws: 
  func keepAlive() throws

  ///
  /// - Throws: AppException
  func deactivate() throws

  ///
  /// - Returns: RequestActivationCodeResponse
  /// - Throws: AppException
  func requestActivationCode() throws -> RequestActivationCodeResponse

  ///
  /// - Parameters:
  ///   - encPassword: 
  /// - Throws: AppException
  func verifyPassword(encPassword: Data) throws

  ///
  /// - Parameters:
  ///   - authUserParam: 
  /// - Returns: AuthUserResponse
  /// - Throws: AppException
  func authenticateUser(authUserParam: AuthUserParam) throws -> AuthUserResponse

  ///
  /// - Parameters:
  ///   - authSelectParam: 
  /// - Returns: AuthSelectResponse
  /// - Throws: AppException
  func getAuthenticationMethods(authSelectParam: AuthSelectParam) throws -> AuthSelectResponse

  ///
  /// - Returns: TList<DeviceOfflineKey>
  /// - Throws: AppException
  func getDeviceOfflineKeys() throws -> TList<DeviceOfflineKey>

  ///
  /// - Parameters:
  ///   - authUserParam: 
  /// - Returns: Int32
  /// - Throws: AppException
  func authConnect(authUserParam: AuthConnectParam) throws -> Int32

  ///
  /// - Parameters:
  ///   - handle: 
  ///   - data: 
  /// - Returns: Data
  /// - Throws: AppException
  func proxyData(handle: Int64, data: Data) throws -> Data

  ///
  /// - Parameters:
  ///   - handle: 
  /// - Throws: 
  func proxyClose(handle: Int64) throws

}

open class AppToServerClient : TClient /* , AppToServer */ {

}

open class AppToServerProcessor /* AppToServer */ {

  typealias ProcessorHandlerDictionary = [String: (Int32, TProtocol, TProtocol, AppToServer) throws -> Void]

  public var service: AppToServer

  public required init(service: AppToServer) {
    self.service = service
  }

}


