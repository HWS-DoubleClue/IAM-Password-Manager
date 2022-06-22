//start Prelogin Page
$(function() {
		$('#preLoginForm\\:serializedAccounts').val(localStorage.getItem('accounts'));
		if (localStorage.getItem('latestView') == null) {
			$('#preLoginForm\\:latestView').val('passwordSafeView');
		} else {
			$('#preLoginForm\\:latestView').val(localStorage.getItem('latestView'));
		}	
		if (localStorage.getItem('hideTutorial') == null) {
			$('#preLoginForm\\:hideTutorial').val(false);
		} else {
			$('#preLoginForm\\:hideTutorial').val(localStorage.getItem('hideTutorial'));
		}
		$("#preLoginForm\\:ok").click();
	});

//End Prelogin Page