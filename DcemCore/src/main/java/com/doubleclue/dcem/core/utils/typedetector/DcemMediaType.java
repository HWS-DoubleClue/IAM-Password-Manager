package com.doubleclue.dcem.core.utils.typedetector;

import com.doubleclue.dcem.core.DcemConstants;

public enum DcemMediaType {
// DO NOT CHANGE TO ORDINAL NUMBERS	
	XHTML("text/html", "html-file.svg"),
	TEXT("text/plain", "txt-file.png"),
	
	GIF("image/gif", "picture-file.svg"),
	JPEG("image/jpeg", "picture-file.svg"),
	PNG("image/png", "picture-file.svg"),
	
	GZ("application/gzip", "zip-file.svg"),
	EXE_MS("application/x-msdownload", "exe-file.svg"),
	PDF("application/pdf", "pdf.svg"),
	TAR("application/x-tar", "zip-file.svg"),
	XML("application/xml", DcemConstants.DEFAULT_FILE_ICON),
	ZIP("application/zip", "zip-file.svg"),
	KEEPASS("application/octet-stream", "keepass-icon.png"),
	BINARY("application/octet-stream", DcemConstants.DEFAULT_FILE_ICON),
	
	WORD("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "microsoft-word.svg"),
	XLS("application/vnd.ms-excel", "microsoft-excel.svg"),
	XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "microsoft-excel.svg"),
	PPT("application/vnd.ms-powerpoint", "powerpoint-file.png"),
	PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "powerpoint-file.png"),
	ODT ("application/vnd.oasis.opendocument.text",  "word-file.png"),
	MP4 ("video/mp4", "mp4.svg"),
	QuickTime ("video/quicktime", "mp4.svg");

	
	
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
		int ind = mediaType.indexOf(';');
		if (ind != -1) {
			mediaType = mediaType.substring(0, ind);
		}
		mediaType = mediaType.toLowerCase();
		for (DcemMediaType dcemMediaType : DcemMediaType.values()) {
			if (dcemMediaType.getMediaType().equals(mediaType)) {
				return dcemMediaType;
			}
		}
		return null;
	}

	public String getIconName() {
		return iconName;
	}
	
	public String getIconResource() {
		return DcemConstants.ICONS_16_PATH + iconName;
	}
	public String getIconResourcePath() {
		return "icons/16x16/" + iconName;
	}

}
