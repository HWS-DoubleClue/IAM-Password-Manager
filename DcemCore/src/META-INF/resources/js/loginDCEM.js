$(document).ready(function() {
	$('#loginForm\\:rpId').val(window.location.hostname);
	$('#loginForm\\:mgtActiveView').val(localStorage.getItem('mgtActiveView'));
	$('#loginForm\\:mgtUserSettings').val(localStorage.getItem('userSettings'));
	bdcLoc();
});

function startFidoAuthentication(requestJson) {
	if (checkFidoCompatibility()) {
		var request = JSON.parse(requestJson);
		webAuthnGet(request, fidoAuthCallback, fidoErrorCallback);
	}
}

function checkFidoCompatibility() {
	if (!isBrowserWebAuthnCompatible() || !isWebAuthnApiAccessible()) {
		fidoErrorCallback(new Error(
				"#{AdminMsg['mfalogin.error.fido.notSupported']}"));
		return false;
	}
	return true;
}

function fidoAuthCallback(response) {
	$('#loginForm\\:fidoResponse').val(response);
	finishFidoAuthentication();
}

function fidoErrorCallback(err) {
	$('#loginForm\\:fidoError').val(err.message);
	showFidoError();
}

// workaround for Chrome auto-submitting on autofill
$.extend($.ui.keyCode, {
	NUMPAD_ENTER : 108
});