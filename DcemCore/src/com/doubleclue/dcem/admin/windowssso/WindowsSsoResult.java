package com.doubleclue.dcem.admin.windowssso;

import java.util.Arrays;
import java.util.List;

public class WindowsSsoResult {

	WindowsSsoResultType resultType;

	/** The fqn. */
	private String fqn;

	/** The sid. */
	private byte[] sid;

	/** The sid string. */
	private String sidString;

	/** The groups. */
	private List<String> groups;

	public WindowsSsoResult() {
	}

	public WindowsSsoResult(WindowsSsoResultType resultType) {
		this.resultType = resultType;
	}

	public WindowsSsoResultType getResultType() {
		return resultType;
	}

	public void setResultType(WindowsSsoResultType resultType) {
		this.resultType = resultType;
	}

	public String getFqn() {
		return fqn;
	}

	public void setFqn(String fqn) {
		this.fqn = fqn;
	}

	public byte[] getSid() {
		return sid;
	}

	public void setSid(byte[] sid) {
		this.sid = sid;
	}

	public String getSidString() {
		return sidString;
	}

	public void setSidString(String sidString) {
		this.sidString = sidString;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	@Override
	public String toString() {
		return "WindowsSsoResult [resultType=" + resultType + ", fqn=" + fqn + ", sid=" + Arrays.toString(sid)
				+ ", sidString=" + sidString + ", groups=" + groups + "]";
	}
	
	

}
