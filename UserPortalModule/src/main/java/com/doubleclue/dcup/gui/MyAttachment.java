package com.doubleclue.dcup.gui;

public class MyAttachment {
	
	String key;
	int length;
	int ref;
	
		
	public MyAttachment() {
		super();
	}

	public MyAttachment(String key, int lenght ,int ref) {
		super();
		this.key = key;
		this.length = lenght;
		this.ref = ref ;
	}
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getRef() {
		return ref;
	}

	public void setRef(int ref) {
		this.ref = ref;
	}
}
