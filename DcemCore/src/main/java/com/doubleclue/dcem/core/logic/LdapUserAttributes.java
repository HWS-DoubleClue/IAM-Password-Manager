package com.doubleclue.dcem.core.logic;

public class LdapUserAttributes {
	
	String firstName;
	String dn;
	String lastGivenName;
	String email;
	String telephone;
	String mobile;
	
	public LdapUserAttributes() {
		
	
	}
	

	public LdapUserAttributes(String firstName, String lastGivenName, String dn,  String email, String telephone, String mobile) {
		super();
		this.firstName = firstName;
		this.lastGivenName = lastGivenName;
		this.email = email;
		this.dn = dn;
		this.telephone = telephone;
		this.mobile = mobile;
	}	
	
	

}
