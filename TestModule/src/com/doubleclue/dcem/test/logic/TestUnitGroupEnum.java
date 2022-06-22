package com.doubleclue.dcem.test.logic;

public enum TestUnitGroupEnum {
	// Important: Always list subgroups after the corresponding parent group
	
	// Groups on first level
	ADD_USER("Add Users", "Group of test that add users to dcem", null),
	LOGIN_AND_ACTIVATION("Login and Activation", "Group of Login and Activation tests ", null),
	MODULES("Modules", "Group of Module Test Units", null),
	REST("Rest", "Group of units performing REST tests", null),
	SELENIUM("Selenium", "Group of tests using Selenium", null),
	
	// Subgroups
	RADIUS("Radius", "Group of tests for the Radius Module", MODULES),
	USERPORTAL("Userportal", "Group of tests for Userportal", MODULES);

	private final String value;
	private final String description;
	TestUnitGroupEnum parent;
	
	private TestUnitGroupEnum(String value, String description, TestUnitGroupEnum parentGroup) {
		this.value = value;
		this.parent = parentGroup;
		this.description = description;
	}
	
	@Override
	public String toString() {
		return value;
	}

	public TestUnitGroupEnum getParent() {
		return parent;
	}

	public void setParent(TestUnitGroupEnum parent) {
		this.parent = parent;
	}

	public String getDescription() {
		return description;
	}
	
}
