package com.doubleclue.dcup.gui;

public enum PasswordSafePages {
	
	INFO ("passwordSafeInfo.xhtml"),
	CHOOSE_FILE("passwordSafeChooseFile.xhtml"),
	FILE_PANEL("passwordSafeFilePanel.xhtml");
	
	String xhtmlPage;

	private PasswordSafePages(String xhtmlPage) {
		this.xhtmlPage = xhtmlPage;
	}

	public String getXhtmlPage() {
		return xhtmlPage;
	}

	public void setXhtmlPage(String xhtmlPage) {
		this.xhtmlPage = xhtmlPage;
	}

}
