package com.doubleclue.dcem.core.logic;

public enum FileContentType {
	pdf("application/pdf"),
	xml("text/xml"),
	html("text/html"),
	css("text/css"),
	csv("text/csv"),
	js("text/javascript"),
	txt("text/plain"),
	docx("text/plain"),

	png("image/png"),
	jpg("image/jpeg"),
	gif("image/gif"),
	tiff("image/tiff"),
	svg("image/svg"),

	mp4("video/mp4"),
	mpeg("video/mpeg"),
	mov("video/quicktime"),
	webm("video/webm"),

	zip("application/zip");

	private String value;

	FileContentType(String value) {
		if(value == null) {
			value = "text/plain";
		}
		this.value = value;
	}

	public String getValue() {
		if(value == null) {
			value = "text/plain";
		}
		return value;
	}

}
