package com.doubleclue.dcem.core.utils.typedetector;

public enum DcemMediaType {
	
	HTML("text/html"),
	TXT("text/plain"),
	
	GIF("image/gif"),
	JPEG("image/jpeg"),
	PNG("image/png"),
	
	GZ("application/gzip"),
	EXE_MS("application/x-msdownload"),
	PDF("application/pdf"),
	TAR("application/x-tar"),
	XML("application/xml"),
	ZIP("application/zip");
	
	private String mediaType;

	private DcemMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getMediaType() {
		return mediaType;
	}
	
}
