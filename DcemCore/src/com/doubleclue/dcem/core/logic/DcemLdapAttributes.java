package com.doubleclue.dcem.core.logic;

import com.doubleclue.dcem.core.entities.DcemUser;

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
	String country;
	String department;
	String jobTitle;
	String managerId;
	byte [] objectGuid;
	byte [] photo;
	

	public DcemLdapAttributes() {

	}

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

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	@Override
	public String toString() {
		return "DcemLdapAttributes [firstName=" + firstName + ", dn=" + dn + ", lastGivenName=" + lastGivenName + ", email=" + email + ", telephone="
				+ telephone + ", mobile=" + mobile + ", displayName=" + displayName + ", userPrincipalName=" + userPrincipalName + ", preferredLanguage="
				+ preferredLanguage + ", country=" + country + ", department=" + department + "]";
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getManagerId() {
		return managerId;
	}

	public void setManagerId(String managerId) {
		this.managerId = managerId;
	}

	
}
