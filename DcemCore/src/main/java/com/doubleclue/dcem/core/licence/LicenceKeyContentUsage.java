package com.doubleclue.dcem.core.licence;

public class LicenceKeyContentUsage {

	int maxUsers;
	long cloudSafeStoageMb;	
	LicenceKeyContent keyContent; 

	public LicenceKeyContentUsage() {
		
	}

	

	public long getCloudSafeStoageMb() {
		return cloudSafeStoageMb;
	}

	public void setCloudSafeStoageMb(long cloudSafeStoageMb) {
		this.cloudSafeStoageMb = cloudSafeStoageMb;
	}

	public LicenceKeyContent getKeyContent() {
		return keyContent;
	}

	public void setKeyContent(LicenceKeyContent keyContent) {
		this.keyContent = keyContent;
	}

	public LicenceKeyContentUsage(int maxUsers, long cloudSafeStoageMb, LicenceKeyContent keyContent) {
		super();
		this.maxUsers = maxUsers;
		this.cloudSafeStoageMb = cloudSafeStoageMb;
		this.keyContent = keyContent;
	}



	public int getMaxUsers() {
		return maxUsers;
	}



	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}
	
	

	

	
	
}
