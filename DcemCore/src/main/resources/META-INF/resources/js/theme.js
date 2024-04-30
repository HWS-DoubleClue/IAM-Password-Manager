$("#theme-switcher").on("change", function() {
	$("html").toggleClass("dark-theme");
	if ($("#theme-switcher").prop("checked") === true) {
		localStorage.setItem("theme", "dark");
	} else {
		localStorage.setItem("theme", "light");
	}
});
