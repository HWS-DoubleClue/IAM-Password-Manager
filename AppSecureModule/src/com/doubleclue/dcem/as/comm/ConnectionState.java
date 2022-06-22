package com.doubleclue.dcem.as.comm;

public enum ConnectionState {

	logoff,
	midLogin,
	loggedIn,
	init,
	serverSignature,
	messagePending,
	disconnected,
	disconnecting,
	rpClientPassThrough,
	rpClientOpen,
	rpClientDisconnected,
	rpDcemLoginProcess,
	rpDcemLoggedIn,
	authenticated,
	invalidTenant,
	authenticatedInProgress,
	loggedInPasswordLess,
	AuthConnect;

}
