package com.doubleclue.dcem.setup.logic;

public class ModuleMigrationVersion {

	String id;
	String name;
	String currentVersion;
	String updateToVersion;
	boolean masterOnly;

	public ModuleMigrationVersion(String id, String name, String currentVersion, String updateToVersion, boolean masterOnly) {
		super();
		this.id = id;
		this.name = name;
		this.currentVersion = currentVersion;
		this.updateToVersion = updateToVersion;
		this.masterOnly = masterOnly;
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	public String getUpdateToVersion() {
		return updateToVersion;
	}

	public void setUpdateToVersion(String updateToVersion) {
		this.updateToVersion = updateToVersion;
	}

	public boolean isMasterOnly() {
		return masterOnly;
	}

	public void setMasterOnly(boolean masterOnly) {
		this.masterOnly = masterOnly;
	}
}
