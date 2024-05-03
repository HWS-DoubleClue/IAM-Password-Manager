$("#theme-switcher").on("click", function() {
	$("html").toggleClass("dark-theme");
	if ($("html").hasClass("dark-theme")) {
		localStorage.setItem("theme", "dark");
	} else {
		localStorage.setItem("theme", "light");
	}
});
