package com.doubleclue.dcem.as.logic;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;

public class CloudSafeDto {
	
	private int id;
	private boolean isFolder;

	public CloudSafeDto() {
		super();
	}

	public CloudSafeDto(int id, boolean isFolder) {
		super();
		this.id = id;
		this.isFolder = isFolder;	
	}

	public CloudSafeDto(CloudSafeEntity cloudSafeEntity) {
		this.id = cloudSafeEntity.getId();
		this.isFolder = cloudSafeEntity.isFolder();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	@Override
	public String toString() {
		return "CloudSafeDto [id=" + id + ", isFolder=" + isFolder + "]";
	}
}
