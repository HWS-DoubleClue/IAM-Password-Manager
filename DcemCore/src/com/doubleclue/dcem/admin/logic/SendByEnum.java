package com.doubleclue.dcem.admin.logic;

/**
 * Gets or Sets sendBy
 */
public enum SendByEnum {
	NONE("NONE"),

	SMS("SMS"),

	EMAIL("EMAIL"),
	
	PRIVAT_EMAIL("Private EMAIL");

	private String value;

	SendByEnum(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
