package com.doubleclue.utils;

public class FileContent {
	
	String name;
	byte [] content;
	
	
	
	public FileContent(String name, byte[] content) {
		super();
		this.name = name;
		this.content = content;
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}

}
