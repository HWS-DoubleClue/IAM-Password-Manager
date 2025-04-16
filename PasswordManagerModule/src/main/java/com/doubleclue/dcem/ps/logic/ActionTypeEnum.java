package com.doubleclue.dcem.ps.logic;

public enum ActionTypeEnum {

	button("Button"),  // lower case to be compatible with JavaScript
	input("Input Field"),
	delay("Delay");

	private String displayName;

	private ActionTypeEnum(String name) {
		this.displayName = name;
	}

	public String getDisplayName() {
		return displayName;
	}
}
