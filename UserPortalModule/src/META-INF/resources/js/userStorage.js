//
$(function() {
	$('#userStorageForm\\:psHistory').val(localStorage.getItem('psHistory.' + $('#userId').text()));
	$("#userStorageForm\\:ok").click();
});

