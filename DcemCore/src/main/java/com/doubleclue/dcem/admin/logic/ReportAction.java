package com.doubleclue.dcem.admin.logic;

public enum ReportAction {
	Login,
	Logout,
	Activation,
	Deactivation,
	Login_Signature,
	GetTemplate,
	MessageResponse,
	Disconnected,
	ChangePassword,
	Server_Signature,
	RestAddMessage,
	RequestActivationCode,
	SendPushNotification,
	VerifyPassword,
	WriteCloudSafe,
	GetDomainSdkConfig,
	Authenticate,
	Authenticate_pwd,
	Authenticate_sms,
	Authenticate_voice,
	Authenticate_otp,
	Authenticate_motp,
	Authenticate_push,
	Authenticate_qrcode,
	Authenticate_fido,
	SamlValidation,
	GetAuthMethods,
	DeleteUser,
	DeleteDevice,
	GetCloudSafe,
	GetAccessibleCloudDataFilenames,
	VerifyPasscode,
	LoginPasswordLess,
	Authenticate_reconnect,
	ProxyData,
	AuthConnect,
	Licence,
	Login_reconnect,
	Authenticate_NetworkBypass,
}