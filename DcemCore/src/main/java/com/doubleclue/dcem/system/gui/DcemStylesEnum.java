package com.doubleclue.dcem.system.gui;

public enum DcemStylesEnum {
	RedShift("background-color: #fc4e51;border-color: #fc4e51;"),
	LightRedShift("background-color: #f08080;border-color: #f08080;"),
	BlueShift("background-color: #238bfa;border-color: #238bfa;"),
	LightBlueShift("background-color: #7dd1ff;border-color: #7dd1ff;"),
	YellowShift("background-color: #fcd56a;border-color: #fcd56a;color: black;"),
	LightYellowShift("background-color: #ffe49c;border-color: #ffe49c;color: black;"),
	GreenShift("background-color: #7fb051;border-color: #7fb051;"),
	LightGreenShift("background-color: #9cc476;border-color: #9cc476;"),
	OrangeShift("background-color: #ff8426;border-color: #ff8426;"),
	LightOrangeShift("background-color: #ffab45;border-color: #ffab45;"),
	Absence("background-color:#adadad;");

	private final String value;

	DcemStylesEnum(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}

	public String getValue() {
		return value;
	}

}
