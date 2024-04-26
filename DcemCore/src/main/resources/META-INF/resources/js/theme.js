const themeSwitcher = document.getElementById("theme-switcher");
themeSwitcher.addEventListener("change", function() {
	if (themeSwitcher.checked) {
		document.documentElement.classList.add("dark-theme");
		localStorage.setItem("theme", "dark");
	} else {
		document.documentElement.classList.remove("dark-theme");
		localStorage.setItem("theme", "light");
	}
});