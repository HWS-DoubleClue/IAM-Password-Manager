package com.doubleclue.dcem.core.utils;

public class KeyValuePair {
	String key;
	String value;
	
	public KeyValuePair() {
		
	}
	
	public KeyValuePair(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

}
