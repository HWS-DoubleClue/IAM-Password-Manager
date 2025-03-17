package com.doubleclue.dcem.as.logic;

import java.io.File;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;

public class CloudSafeUploadFile {

	public String fileName;
	public File file;
	private CloudSafeEntity cloudSafeEntity;

	public CloudSafeUploadFile(String fileName, File file, CloudSafeEntity cloudSafeEntity) {
		this.fileName = fileName;
		this.file = file;
		this.cloudSafeEntity = cloudSafeEntity;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public CloudSafeEntity getCloudSafeEntity() {
		return cloudSafeEntity;
	}

	public void setCloudSafeEntity(CloudSafeEntity cloudSafeEntity) {
		this.cloudSafeEntity = cloudSafeEntity;
	}
	

}