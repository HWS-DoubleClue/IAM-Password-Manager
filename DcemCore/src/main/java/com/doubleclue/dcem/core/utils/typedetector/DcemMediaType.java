package com.doubleclue.dcem.core.utils.typedetector;

import com.doubleclue.dcem.core.DcemConstants;

public enum DcemMediaType {
// DO NOT CHANGE TO ORDINAL NUMBERS	
	XHTML("text/html", "html-file.svg", "html"),
	TEXT("text/plain", "txt-file.png", "txt"),
	
	GIF("image/gif", "picture-file.svg", "gif"),
	JPEG("image/jpeg", "picture-file.svg", "jpeg"),
	PNG("image/png", "picture-file.svg", "png"),
	
	GZ("application/gzip", "zip-file.svg", "gz"),
	EXE_MS("application/x-msdownload", "exe-file.svg", "exe"),
	PDF("application/pdf", "pdf.svg", "pdf"),
	TAR("application/x-tar", "zip-file.svg", "tar"),
	XML("application/xml", DcemConstants.DEFAULT_FILE_ICON, "xml"),
	ZIP("application/zip", "zip-file.svg", "zip"),
	KEEPASS("application/octet-stream", "keepass-icon.png", "kdbx"),
	BINARY("application/octet-stream", DcemConstants.DEFAULT_FILE_ICON, "bin"),
	
	WORD("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "microsoft-word.svg", "docx"),
	XLS("application/vnd.ms-excel", "microsoft-excel.svg", "xls"),
	XLSX("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "microsoft-excel.svg", "xlsx"),
	PPT("application/vnd.ms-powerpoint", "powerpoint-file.png", "ppt"),
	PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "powerpoint-file.png", "pttx"),
	ODT ("application/vnd.oasis.opendocument.text",  "word-file.png", "odt"),
	MP4 ("video/mp4", "mp4.svg", "mp4"),
	QuickTime ("video/quicktime", "mp4.svg", "quicktime"),
	SVG ("image/svg+xml", "svg-svgrepo-com.svg", "svg"),
	Unknown ("", "file-unknow-svgrepo-com.svg", ""),
	Folder ("", "folder-svgrepo-com.svg", ""),
	Mail ("", "mail-svgrepo-com.svg", "eml");
	
	
	private String mediaType;
	private String iconName;
	private String nameExtension;

	private DcemMediaType(String mediaType, String iconName, String extension) {
		this.mediaType = mediaType;
		this.iconName = iconName;
		this.nameExtension = extension;
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
		return Unknown;
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

	public String getNameExtension() {
		return nameExtension;
	}

}
