package com.doubleclue.dcem.as.dm;

import java.io.File;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;

public class UploadDocument {

	String name;
	File file;
	CloudSafeEntity parentFolder;
	CloudSafeEntity recoverFrom;
	String webPath;
	UploadDocumentStatus status = UploadDocumentStatus.Waiting;
	Exception exception;
	DcemMediaType dcemMediaType;
	long seconds;
	long startSeconds;
	boolean overwrite;

	public UploadDocument(String name, File file, CloudSafeEntity parentFolder, DcemMediaType dcemMediaType) {
		super();
		this.name = name;
		this.file = file;
		this.parentFolder = parentFolder;
		this.dcemMediaType = dcemMediaType;
	}

	public String getPathName() {
		try {
			if (parentFolder != null && parentFolder.getName().equals(DcemConstants.CLOUD_SAFE_ROOT)) {
				return name;
			}
			return parentFolder.getName() + CloudSafeLogic.FOLDER_SEPERATOR + name;
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public CloudSafeEntity getParentFolder() {
		return parentFolder;
	}

	public void setParentFolder(CloudSafeEntity parentFolder) {
		this.parentFolder = parentFolder;
	}

	public UploadDocumentStatus getStatus() {
		return status;
	}

	public void setStatus(UploadDocumentStatus status) {
		this.status = status;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public DcemMediaType getDcemMediaType() {
		return dcemMediaType;
	}

	public void setDcemMediaType(DcemMediaType dcemMediaType) {
		this.dcemMediaType = dcemMediaType;
	}

	public String getStatusIcon() {
		switch (status) {
		case Processing:
			return "svg/progress-svgrepo-com.svg";
		case Uploaded:
			return "svg/check-box-svgrepo-com.svg";
		case Error:
			return "svg/x-cross-red-svgrepo-com.svg";
		case Waiting:
			return "svg/pause-svgrepo-com.svg";
		}
		return null;
	}

	public long getSeconds() {
		return seconds;
	}

	public void setSeconds(long seconds) {
		this.seconds = seconds;
	}

	public long getStartSeconds() {
		return startSeconds;
	}

	public void setStartSeconds(long startSeconds) {
		this.startSeconds = startSeconds;
	}

	public String getWebPath() {
		return webPath;
	}

	public void setWebPath(String webPath) {
		this.webPath = webPath;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public CloudSafeEntity getRecoverFrom() {
		return recoverFrom;
	}

	public void setRecoverFrom(CloudSafeEntity recoverFrom) {
		this.recoverFrom = recoverFrom;
	}

}
