// Start Select Login
		$(function() {
				var options = {};
					Fingerprint2.get(options, function(components) {
					var values = components.map(function(component) {
					return component.value
						});
				var murmur = Fingerprint2.x64hash128(values.join(''),31);
				$('#selectLoginForm\\:browserFingerprint').val(murmur);
					});
				$('#selectLoginForm\\:serializedAccounts').val(localStorage.getItem('accounts'));
				});

		function setLocalStorageValue(key, value) {
			localStorage.setItem(key, value);
		}

		function removeLocalStorage(key) {
			localStorage.removeItem(key);
		}
		
// End Select Login