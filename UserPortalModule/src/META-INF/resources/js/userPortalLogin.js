//Start Login page of user portal 
$(function() {
	bdcLoc();
	$('#loginForm\\:latestView').val(localStorage.getItem('latestView'));
	$('#loginForm\\:rpId').val(window.location.hostname);
	if (localStorage.getItem('qrCodeUseState') == null) {
		$('#loginForm\\:qrCodeUseState').val(false);
	} else {
		$('#loginForm\\:qrCodeUseState').val(localStorage.getItem('qrCodeUseState'));
	}
	if (localStorage.getItem('qrCodeUseState') == 'true') {
		$('#loginForm\\:requestQrCode').click();
	}

});

function updatePsHistoryValueForUser(userid) {
	$('#loginForm\\:psHistory').val(localStorage.getItem('psHistory.' + userid));
	updatePsHistory();
}

function setLocalStorageValue(key, value) {
	localStorage.setItem(key, value);
}

function removeLocalStorage(key) {
	localStorage.removeItem(key);
}

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
	NUMPAD_ENTER: 108
});

// End Login page of user portal 
