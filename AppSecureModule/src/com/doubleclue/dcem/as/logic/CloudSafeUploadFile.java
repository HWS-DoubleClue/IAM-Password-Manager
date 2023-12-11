package com.doubleclue.dcem.as.logic;

import java.io.File;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;

public class CloudSafeUploadFile {

	public String fileName;
	public File file;
	public CloudSafeEntity cloudSafeEntity;

	public CloudSafeUploadFile(String fileName, File file, CloudSafeEntity cloudSafeEntity) {
		this.fileName = fileName;
		this.file = file;
		this.cloudSafeEntity = cloudSafeEntity;
	}

}