

    PrimeFaces.locales['es'] = {
        closeText: 'Cerrar',
        prevText: 'Anterior',
        nextText: 'Siguiente',
        monthNames: ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio', 'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'],
        monthNamesShort: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'],
        dayNames: ['Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'],
        dayNamesShort: ['Dom', 'Lun', 'Mar', 'Mie', 'Jue', 'Vie', 'Sab'],
        dayNamesMin: ['D', 'L', 'M', 'X', 'J', 'V', 'S'],
        weekHeader: 'Semana',
        firstDay: 1,
        isRTL: false,
        showMonthAfterYear: false,
        yearSuffix: '',
        timeOnlyTitle: 'Sólo hora',
        timeText: 'Tiempo',
        hourText: 'Hora',
        minuteText: 'Minuto',
        secondText: 'Segundo',
        millisecondText: 'Milisegundo',
        currentText: 'Fecha actual',
        ampm: false,
        month: 'Mes',
        week: 'Semana',
        day: 'Día',
        allDayText: 'Todo el día',
        today: 'Hoy',
        clear: 'Claro'
    };
    PrimeFaces.locales ['de'] = {
        closeText: 'Schließen',
        prevText: 'Zurück',
        nextText: 'Weiter',
        monthNames: ['Januar', 'Februar', 'März', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'],
        monthNamesShort: ['Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'],
        dayNames: ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],
        dayNamesShort: ['Son', 'Mon', 'Die', 'Mit', 'Don', 'Fre', 'Sam'],
        dayNamesMin: ['S', 'M', 'D', 'M ', 'D', 'F ', 'S'],
        weekHeader: 'Woche',
        firstDay: 1,
        isRTL: false,
        showMonthAfterYear: false,
        yearSuffix: '',
        timeOnlyTitle: 'Nur Zeit',
        timeText: 'Zeit',
        hourText: 'Stunde',
        minuteText: 'Minute',
        secondText: 'Sekunde',
        millisecondText: 'Millisekunde',
        currentText: 'Aktuelles Datum',
        ampm: false,
        month: 'Monat',
        week: 'Woche',
        day: 'Tag',
        allDayText: 'Ganzer Tag',
        today: 'Heute',
        clear: 'Löschen'
    };

    function dateTemplateFunc(date) {
        return '<span style="background-color:' + ((date.day < 21 && date.day > 10) ? '#81C784' : 'inherit') + ';border-radius:50%;width: 2.5rem;height: 2.5rem;line-height: 2.5rem;display: flex;align-items: center;justify-content: center;">' + date.day + '</span>';
    }


// Start index page
function openNav() {
	document.getElementById("closeButton").style.display = "table-cell";
	document.getElementById("sidemenu").style.width = "15.75em";
	document.getElementById("sidemenu").style.transition = "0.5s all";
	document.getElementById("contentPart").style.marginLeft = "15.75em";
	document.getElementById("contentPart").style.transition = "0.5s all";
	document.getElementById("sidebarbtn").style.display = "none";
}

function closeNav() {
	document.getElementById("closeButton").style.display = "none";
	document.getElementById("sidemenu").style.width = "0";
	document.getElementById("sidemenu").style.transition = "0.5s all";
	document.getElementById("contentPart").style.marginLeft = "0";
	document.getElementById("contentPart").style.transition = "0.5s all";
	document.getElementById("sidebarbtn").style.display = "table-cell";
}

function setLocalStorageValue(key, value) {
	localStorage.setItem(key, value);
}

function removeLocalStorage(key) {
	localStorage.removeItem(key);
}

window.onload = function getLocalStorgeSideMenuStatus() {
	if (localStorage.getItem('sideMenuClose') == 'true') {
		closeNav();
		removeLocalStorage('sideMenuOpen');

	} else {
		openNav();
		removeLocalStorage('sideMenuClose');
	}
}
// End index page

// start perferences
function switchPwdVisibleState(eyeBtn) {
	var field = $(eyeBtn).siblings('input');
	var button = $(eyeBtn).children('i')[0];
	if (field.attr('type') === 'password') {
		$(field).attr('type', 'text');
		$(button).removeClass('fa fa-eye');
		$(button).addClass('fa fa-eye-slash');
	} else {
		$(field).attr('type', 'password');
		$(button).removeClass('fa fa-eye-slash');
		$(button).addClass('fa fa-eye');
	}
}
		// end perferences
