		// Start Prelogin page
		$(document).ready(function() {
			$('#preLoginForm\\:mgtActiveView').val(localStorage.getItem('mgtActiveView'));
			$('#preLoginForm\\:serializedAccounts').val(localStorage.getItem('accounts'));
			$('#preLoginForm\\:mgtUserSettings').val(localStorage.getItem('userSettings'));
			$("#preLoginForm\\:ok").click();
});
		
		// End Prelogin page