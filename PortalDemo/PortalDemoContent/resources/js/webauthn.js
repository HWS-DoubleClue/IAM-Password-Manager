function isBrowserWebAuthnCompatible() {
	return window.PublicKeyCredential !== undefined;
}

function isWebAuthnApiAccessible() {
	return navigator.credentials !== undefined;
}

function webAuthnCreate(request, successCallback, errorCallback) {
		
	// convert base64url strings to ArrayBuffers
	request.challenge = base64UrlToArrayBuffer(request.challenge);
	request.user.id = base64UrlToArrayBuffer(request.user.id);
	request.excludeCredentials.forEach(function(e) {
		e.id = base64UrlToArrayBuffer(e.id);
	});
	
	// call authenticator
	navigator.credentials.create({ publicKey: request })
    .then(function (attestation) {
    	successCallback(attestationToJson(attestation));
	}).catch(function (err) {
		errorCallback(err);
	});
}

function webAuthnGet(request, successCallback, errorCallback) {
	
	// convert base64url strings to ArrayBuffers
	var publicKey = request.publicKeyCredentialRequestOptions;
	publicKey.challenge = base64UrlToArrayBuffer(publicKey.challenge);
	publicKey.allowCredentials.forEach(function(e) {
		e.id = base64UrlToArrayBuffer(e.id);
	});
	
	// call authenticator
	navigator.credentials.get({ publicKey: publicKey })
	.then(function (attestation) {
		successCallback(attestationToJson(attestation));
	}).catch(function (err) {
		errorCallback(err);
	});
}

function attestationToJson(attestation) {
	
	var res = attestation.response;
	var serRes = {
		clientDataJSON: arrayBufferToBase64Url(res.clientDataJSON)
	};
	
	if (res.attestationObject != undefined) { // registration
		serRes.attestationObject = arrayBufferToBase64Url(res.attestationObject);
	} else { // authentication
		serRes.authenticatorData = arrayBufferToBase64Url(res.authenticatorData);
		serRes.signature = arrayBufferToBase64Url(res.signature);
		serRes.userHandle = arrayBufferToBase64Url(res.userHandle);
	}
	
	var serialisable = {
		id: attestation.id,
		type: attestation.type,
		response: serRes,
		clientExtensionResults: {}
   	};
	
	return JSON.stringify(serialisable);
}

function strToArrayBuffer(str) {
	var buf = new ArrayBuffer(str.length);
	var bufView = new Uint8Array(buf);
	var length = str.length;
	for (var i = 0; i < length; i++) {
		bufView[i] = str.charCodeAt(i);
	}
	return buf;
}

function arrayBufferToStr(buf) {
	return String.fromCharCode.apply(null, new Uint8Array(buf));
}

function base64ToBase64Url(str) {
	return str.split('=')[0].replaceAll('+', '-').replaceAll('/', '_');
}

function base64UrlToBase64(str) {
	var base64 = str.replaceAll('-', '+').replaceAll('_', '/');
	switch (base64.length % 4)
    {
        case 2: base64 += "=="; break;
        case 3: base64 += "="; break;
        default: break;
    }
	return base64;
}

function arrayBufferToBase64Url(buf) {
	return base64ToBase64Url(btoa(arrayBufferToStr(buf)));
}

function base64UrlToArrayBuffer(str) {
	return strToArrayBuffer(atob(base64UrlToBase64(str)));
}

String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.split(search).join(replacement);
};