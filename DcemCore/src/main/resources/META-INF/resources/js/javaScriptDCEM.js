

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
PrimeFaces.locales['de'] = {
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


function setLocalStorageValue(key, value) {
	localStorage.setItem(key, value);
}

function removeLocalStorage(key) {
	localStorage.removeItem(key);
}

function toggleNav() {
	$("#sidemenu").toggleClass("close");
	$("#contentPart").toggleClass("extended");
	setLocalStorageValue('sideMenuClose', $("#sidemenu").hasClass("close"));
}

window.onload = function getLocalStorgeSideMenuStatus() {
	if (localStorage.getItem('sideMenuClose') == 'true') {
		$("#sidemenu").addClass("close");
		$("#contentPart").addClass("extended");

	} else {
		$("#sidemenu").removeClass("close");
		$("#contentPart").removeClass("extended");
	}
}


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


function startTimer(duration) {
	var timer = duration, minutes, seconds;
	//  console.log("duration " + timer);
	setInterval(function() {
		minutes = parseInt(timer / 60, 10);
		seconds = parseInt(timer % 60, 10);

		minutes = minutes < 10 ? "0" + minutes : minutes;
		seconds = seconds < 10 ? "0" + seconds : seconds;
		//        console.log(timer);
		if (timer < 600) {  // show time if less than 10 minutes
			document.getElementById("title:sessionTimeout").innerText = minutes + ":" + seconds;
		}
		timer = timer - 10;
		if (timer < 0) {
			timer = duration;
		}
	}, 10000);
}




// end perferences
