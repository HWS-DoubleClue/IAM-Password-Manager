package com.doubleclue.dcem.core.logic;

public class LoginAuthenticator {

	String name;
	String password;
	
	public LoginAuthenticator () {
		
	}
		
	public LoginAuthenticator(String name, String password) {
		this.name = name;
		this.password = password;
	}
	
	
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	




}
