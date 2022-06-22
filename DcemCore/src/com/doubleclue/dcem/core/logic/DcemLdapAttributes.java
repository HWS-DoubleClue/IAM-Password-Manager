package com.doubleclue.dcem.core.logic;

public class DcemLdapAttributes {

	String firstName;
	String dn;
	String lastGivenName;
	String email;
	String telephone;
	String mobile;
	String displayName;
	String userPrincipalName;
	String preferredLanguage;
	byte [] objectGuid;
	byte [] photo;
	

	public DcemLdapAttributes() {

	}

//	public DcemLdapAttributes(String firstName, String lastGivenName, String dn, String email, String telephone, String mobile,
//			String displayName, String userPrincipalName, byte [] objectGuid, String preferredAttributes) {
//		super();
//		this.firstName = firstName;
//		this.lastGivenName = lastGivenName;
//		this.email = email;
//		this.dn = dn;
//		this.telephone = telephone;
//		this.mobile = mobile;
//		this.displayName = displayName;
//		this.userPrincipalName = userPrincipalName;
//		this.objectGuid = objectGuid;
//		this.preferredLangauge = preferredAttributes;
//		this.photo = photo;
//	}

	public String getDisplayName() {
		if (displayName == null) {
			StringBuffer sb = new StringBuffer();
			if (firstName != null) {
				sb.append(firstName);
			}
			if (lastGivenName != null) {
				if (sb.length() > 0) {
					sb.append(" ");
				}
				sb.append(lastGivenName);
			}
			return sb.toString();
		} else {
			return displayName;
		}
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getDn() {
		return dn;
	}

	public void setDn(String dn) {
		this.dn = dn;
	}

	public String getLastGivenName() {
		return lastGivenName;
	}

	public void setLastGivenName(String lastGivenName) {
		this.lastGivenName = lastGivenName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getUserPrincipalName() {
		return userPrincipalName;
	}

	public void setUserPrincipalName(String userPrincipalName) {
		this.userPrincipalName = userPrincipalName;
	}

	public byte[] getObjectGuid() {
		return objectGuid;
	}

	public void setObjectGuid(byte[] objectGuid) {
		this.objectGuid = objectGuid;
	}

	
	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public String getPreferredLanguage() {
		return preferredLanguage;
	}

	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}
}
