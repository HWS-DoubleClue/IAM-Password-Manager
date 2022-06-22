package com.doubleclue.dcem.core.licence;

import java.util.Date;

import com.doubleclue.dcem.core.DcemConstants;

public class LicenceKeyContent {

	int version = 0;
	String clusterId;
	String customerName;
	boolean trialVersion;
	Date createdOn;
	Date expiresOn;
	String tenantId;
	int maxUsers = DcemConstants.LICENCE_MAX_DEFAULT_USERS;
	String disabledModules;
	boolean passwordSafe = true;
	long cloudSafeStoageMb = 50;
	

	public LicenceKeyContent() {
	}

	public LicenceKeyContent(String clusterId, String customerName, Date expiresOn, String tenantId, int version) {
		this.version = version;
		this.clusterId = clusterId;
		this.customerName = customerName;
		this.createdOn = new Date();
		this.tenantId = tenantId;
		this.expiresOn = expiresOn;
		trialVersion = true;
	}

	public String getClusterId() {
		return clusterId;
	}

	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Date getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(Date expiresOn) {
		this.expiresOn = expiresOn;
	}

	public int getMaxUsers() {
		return maxUsers;
	}

	public void setMaxUsers(int maxUsers) {
		this.maxUsers = maxUsers;
	}

	

	@Override
	public String toString() {
		return "LicenceKeyContent [trialVersion=" + trialVersion + ", expiresOn=" + expiresOn + ", maxUsers=" + maxUsers + ", disabledModules=" + disabledModules +
				", passwordSafe=" + passwordSafe + ", cloudSafeStoageMb=" + cloudSafeStoageMb + "]";
	}

	

	public boolean isPasswordSafe() {
		return passwordSafe;
	}

	public void setPasswordSafe(boolean passwordSafe) {
		this.passwordSafe = passwordSafe;
	}

	public long getCloudSafeStoageMb() {
		return cloudSafeStoageMb;
	}

	public void setCloudSafeStoageMb(long cloudSafeStoageMb) {
		this.cloudSafeStoageMb = cloudSafeStoageMb;
	}

	public boolean isTrialVersion() {
		return trialVersion;
	}

	public void setTrialVersion(boolean trialVersion) {
		this.trialVersion = trialVersion;
	}

	public String getDisabledModules() {
		return disabledModules;
	}

	public void setDisabledModules(String disabledModules) {
		this.disabledModules = disabledModules;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}
	
	
}
