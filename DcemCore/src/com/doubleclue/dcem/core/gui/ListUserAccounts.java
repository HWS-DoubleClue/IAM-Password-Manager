package com.doubleclue.dcem.core.gui;

import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ListUserAccounts {

	
//	@JsonProperty("a2")
//	String browserFingerPrint;

	@JsonProperty("a3")
	LinkedList<UserAccount> accounts = new LinkedList<>();

	@JsonIgnore
	public void removeAccount(UserAccount selectedAccount) {
		if (accounts != null) {
			accounts.remove(selectedAccount);
		}
		
	}

	@JsonIgnore
	public void addAccount (UserAccount userAccount) {
		if (accounts == null) {
			accounts = new LinkedList<UserAccount>();
		}
		accounts.add(userAccount);
		
	}

	public LinkedList<UserAccount> getAccounts() {
		return accounts;
	}

	public void setAccounts(LinkedList<UserAccount> accounts) {
		this.accounts = accounts;
	}
	
	@JsonIgnore
	public String getUser() {
		if (accounts != null && accounts.isEmpty() == false) {
			return accounts.get(0).getUserLoginId();
		} else {
			return "";
		}
	}

}
