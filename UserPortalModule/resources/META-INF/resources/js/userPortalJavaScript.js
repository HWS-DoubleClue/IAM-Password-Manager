// Start welcome page 
function setLocalStorageValue(key, value) {
	localStorage.setItem(key, value);
}

function removeLocalStorage(key) {
	localStorage.removeItem(key);
}

function openNav() {
	document.getElementById("mySidenav").style.width = "13em";
	document.getElementById("contentPart").style.marginLeft = "16em";
	document.getElementById("sidebarbtn").style.visibility = "hidden";
}

function closeNav() {
	document.getElementById("mySidenav").style.width = "0";
	document.getElementById("contentPart").style.marginLeft = "0";
	document.getElementById("sidebarbtn").style.visibility = "visible";
}

window.onload = function getLocalStorgeSideMenuStatus() {
	if (localStorage.getItem('sideMenuPortalClose') == 'true') {
		closeNav();
		removeLocalStorage('sideMenuPortalOpen');

	} else {
		openNav();
		removeLocalStorage('sideMenuPortalClose');
	}
}
// End Welcome Page

function openTab (url) {
	setTimeout(() => {window.open(url, '_blank');})
}

// Start myApplication

function triggerAppHubLogin(action) {
	var isInstalled = document.documentElement.getAttribute('dcem-extension-installed');
	if (isInstalled) {
		let event = new CustomEvent('start-doubleclue-addon', {
			detail: {
				action: action
			}
		});
		document.dispatchEvent(event);
	} else {
		if (navigator.userAgent.indexOf("Chrome") != -1) {
			pluginChromeUnavailableAlert();
		} else if (navigator.userAgent.indexOf("Firefox") != -1) {
			pluginFirefoxUnavailableAlert();
		} else if (navigator.userAgent.indexOf("Safari") != -1) {
			pluginSafariUnavailableAlert();
		} else {
			pluginUnavailableAlert();
		}
	}
}

//End myApplication

//Start myApplication Admin
function triggerAppHubAdminLogin(action) {
	var isInstalled = document.documentElement.getAttribute('dcem-extension-installed');
	if (isInstalled) {
		let event = new CustomEvent('start-doubleclue-addon', {
			detail: {
				admin: action
			}
		});
		document.dispatchEvent(event);
	} else {
		if (navigator.userAgent.indexOf("Chrome") != -1) {
			pluginChromeUnavailableAlert();
		} else if (navigator.userAgent.indexOf("Firefox") != -1) {
			pluginFirefoxUnavailableAlert();
		} else if (navigator.userAgent.indexOf("Safari") != -1) {
			pluginSafariUnavailableAlert();
		} else {
			pluginUnavailableAlert();
		}
	}
}

//Start myApplication Admin
function triggerAppHubCustomAppLogin(action) {
	var isInstalled = document.documentElement.getAttribute('dcem-extension-installed');
	if (isInstalled) {
		let event = new CustomEvent('start-doubleclue-addon', {
			detail: {
				customapp: action
			}
		});
		document.dispatchEvent(event);
	} else {
		if (navigator.userAgent.indexOf("Chrome") != -1) {
			pluginChromeUnavailableAlert();
		} else if (navigator.userAgent.indexOf("Firefox") != -1) {
			pluginFirefoxUnavailableAlert();
		} else if (navigator.userAgent.indexOf("Safari") != -1) {
			pluginSafariUnavailableAlert();
		} else {
			pluginUnavailableAlert();
		}
	}
}

function pluginCallback(response) {
	debugger;
	$('#addAppsForm\\:tabView\\:pluginResponse').val(response);
	finishExecutePlugin();
}
//End myApplicatio Admin

// Start Devices
$(function() {
	$('#deviceForm\\:rpId').val(window.location.hostname);
});

function checkCompatibility() {
	if (!isBrowserWebAuthnCompatible() || !isWebAuthnApiAccessible()) {
		errorCallback(new Error("#{DcupMsg['error.local.FIDO_NOT_SUPPORTED']}"));
		return false;
	}
	return true;
}

function startRegisterEvent(xhr, status, args) {
	if (args.json !== undefined) {
		if (checkCompatibility()) {
			var request = JSON.parse(args.json);
			webAuthnCreate(request, registerCallback, errorCallback);
		}
	}
}

function registerCallback(response) {
	$('#deviceForm\\:regResponse').val(response);
	fidoFinishRegistration();
}

function errorCallback(err) {
	$('#deviceForm\\:regError').val(err.message);
	showError();
}
// End Devices

// Start Keepass

function uploadFile() {
	document.getElementById("uploadBtn").click();
}

function fallbackCopyTextToClipboard(text) {
	var textArea = document.createElement("textarea");
	textArea.value = text;
	document.body.appendChild(textArea);
	textArea.focus();
	textArea.select();

	try {
		var successful = document.execCommand('copy');
		var msg = successful ? 'successful' : 'unsuccessful';
		console.log('Fallback: Copying text command was ' + msg);
	} catch (err) {
		console.error('Fallback: Oops, unable to copy', err);
	}

	document.body.removeChild(textArea);
}
function copyTextToClipboard(text) {
	if (!navigator.clipboard) {
		fallbackCopyTextToClipboard(text);
		return;
	}
	navigator.clipboard.writeText(text).then(function() {
		console.log('Async: Copying to clipboard was successful!');
	}, function(err) {
		console.error('Async: Could not copy text: ', err);
	});
}

/* function handleDrop(event, ui) {
	var droppedCar = ui.draggable;

	droppedCar.fadeOut('fast');
} */
function uploadFile() {
	document.getElementById("processEntryForm:uploadBtn").click();
}
// End Keepass