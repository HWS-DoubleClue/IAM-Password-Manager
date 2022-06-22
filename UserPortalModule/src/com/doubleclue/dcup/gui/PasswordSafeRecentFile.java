package com.doubleclue.dcup.gui;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PasswordSafeRecentFile {

	String name;
	int id;
	String encPassword;
	long timestamp;
	String groupName;

	public PasswordSafeRecentFile() {
		super();
	}

	public PasswordSafeRecentFile(int id ,String name, String encPassword, String groupName) {
		super();
		this.id = id;
		this.name = name;
		this.encPassword = encPassword;
		this.timestamp = System.currentTimeMillis();
		this.groupName = groupName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEncPassword() {
		return encPassword;
	}

	public void setEncPassword(String encPassword) {
		this.encPassword = encPassword;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long epcoh) {
		this.timestamp = epcoh;
	}

	public String getGroup() {
		return groupName;
	}

	public void setGroup(String groupName) {
		this.groupName = groupName;
	}

	@JsonIgnore
	public Date getDate() {
		return new Date(timestamp);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((encPassword == null) ? 0 : encPassword.hashCode());
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PasswordSafeRecentFile other = (PasswordSafeRecentFile) obj;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
