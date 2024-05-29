package com.doubleclue.dcem.system.send;

public class EmailAttachment {
	
	private byte[] attachment;
	private String fileName;
	private String mimeType;
	private boolean dispositionInline;
	private String contentId;
	
	public EmailAttachment() {
	}
	
	public EmailAttachment(byte[] attachment, String fileName, String mimeType) {
		this.attachment = attachment;
		this.fileName = fileName;
		this.mimeType = mimeType;
	}
	
	public EmailAttachment(byte[] attachment, String fileName, String mimeType, boolean dispositionInline, String contentId) {
		this.attachment = attachment;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.dispositionInline = dispositionInline;
		this.contentId = contentId;
	}

	public byte[] getAttachment() {
		return attachment;
	}

	public void setAttachment(byte[] attachment) {
		this.attachment = attachment;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public boolean isDispositionInline() {
		return dispositionInline;
	}

	public void setDispositionInline(boolean dispositionInline) {
		this.dispositionInline = dispositionInline;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

}
