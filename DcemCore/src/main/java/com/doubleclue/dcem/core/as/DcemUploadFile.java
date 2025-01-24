package com.doubleclue.dcem.core.as;

import java.io.File;

import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;

public class DcemUploadFile {

	public String fileName;
	public File file;
	DcemMediaType dcemMediaType;
	public String info;

	public DcemUploadFile(String fileName, File file) {
		this.fileName = fileName;
		this.file = file;
	}
	
	public DcemUploadFile(String fileName, File file, String info) {
		this.fileName = fileName;
		this.file = file;
		this.info = info;
	}
	
	public DcemUploadFile(String fileName, File file, DcemMediaType dcemMediaType, String info) {
		this.fileName = fileName;
		this.file = file;
		this.dcemMediaType = dcemMediaType;
		this.info = info;
	}

}