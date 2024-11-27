package com.doubleclue.dcem.core.utils.typedetector;

import com.doubleclue.dcem.core.DcemConstants;

public enum DcemMediaType {
// DO NOT CHANGE TO ORDINAL NUMBERS	
	HTML("text/html", "html-file.svg"),
	TEXT("text/plain", "txt-file.png"),
	
	GIF("image/gif", "picture-file.svg"),
	JPEG("image/jpeg", "picture-file.svg"),
	PNG("image/png", "picture-file.svg"),
	
	GZ("application/gzip", "zip-file.svg"),
	EXE_MS("application/x-msdownload", "exe-file.svg"),
	PDF("application/pdf", "pdf-file.svg"),
	TAR("application/x-tar", "zip-file.svg"),
	XML("application/xml", DcemConstants.DEFAULT_FILE_ICON),
	ZIP("application/zip", "zip-file.svg"),
	KEEPASS("application/octet-stream", "keepass-icon.png"),
	BINARY("application/octet-stream", DcemConstants.DEFAULT_FILE_ICON),
	
	DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "word-file.png"),
	XLS("application/vnd.ms-excel", "excel-file.png"),
	XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "excel-file.png"),
	PPT("application/vnd.ms-powerpoint", "powerpoint-file.png"),
	PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "powerpoint-file.png"),
	ODT ("application/vnd.oasis.opendocument.text",  "word-file.png"),
	RICH_TEXT("text/html", "html-file.svg");
	
	
	private String mediaType;
	private String iconName;

	private DcemMediaType(String mediaType, String iconName) {
		this.mediaType = mediaType;
		this.iconName = iconName;
	}

	public String getMediaType() {
		return mediaType;
	}
	
	public static DcemMediaType getDcemMediaType (String mediaType) {
		mediaType = mediaType.toLowerCase();
		for (DcemMediaType dcemMediaType : DcemMediaType.values()) {
			if (dcemMediaType.getMediaType().equals(mediaType)) {
				return dcemMediaType;
			}
		}
		return DcemMediaType.BINARY;
	}

	public String getIconName() {
		return iconName;
	}
	
	public String getIconResource() {
		return "icons/16x16/" + iconName;
	}

}
