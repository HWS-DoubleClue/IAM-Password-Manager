package com.doubleclue.dcem.as.logic.cloudsafe;

import java.time.Instant;

import com.doubleclue.dcem.as.logic.DataUnit;

public class DocumentVersion {

	Instant lastModified;
	String versionId;
	long size;
	String eTag;
	boolean latest;

	public DocumentVersion(Instant lastModified, String versionId, long size, String eTag, boolean latest) {
		super();
		this.lastModified = lastModified;
		this.versionId = versionId;
		this.size = size;
		this.eTag = eTag;
		this.latest = latest;
	}

	public Instant getLastModified() {
		return lastModified;
	}

	public void setLastModified(Instant lastModified) {
		this.lastModified = lastModified;
	}

	public String getVersionId() {
		return versionId;
	}

	public void setVersionId(String versionId) {
		this.versionId = versionId;
	}

	public long getSize() {
		return size;
	}

	public String getLengthKb() {
		return DataUnit.getByteCountAsString(size);
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String geteTag() {
		return eTag;
	}

	public void seteTag(String eTag) {
		this.eTag = eTag;
	}

	public boolean isLatest() {
		return latest;
	}

	public void setLatest(boolean latest) {
		this.latest = latest;
	}
}
