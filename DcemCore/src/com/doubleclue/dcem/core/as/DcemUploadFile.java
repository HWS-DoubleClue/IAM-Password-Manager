package com.doubleclue.dcem.core.as;

import java.io.File;

public class DcemUploadFile {

	public String fileName;
	public File file;

	public DcemUploadFile(String fileName, File file) {
		this.fileName = fileName;
		this.file = file;
	}

}