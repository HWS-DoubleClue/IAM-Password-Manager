package com.doubleclue.dcup.logic;

public class WelcomePanels {

	WelcomePanelsEnum welcomePanelsEnum;
	String name;
	String title;
	String imageName;
	String body;
	String cssStyle;
	String pageName;

	public WelcomePanels(WelcomePanelsEnum welcomePanelsEnum) {
		super();
		this.welcomePanelsEnum = welcomePanelsEnum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getCssStyle() {
		return cssStyle;
	}

	public void setCssStyle(String cssStyle) {
		this.cssStyle = cssStyle;
	}

	public WelcomePanelsEnum getWelcomePanelsEnum() {
		return welcomePanelsEnum;
	}

	public void setWelcomePanelsEnum(WelcomePanelsEnum welcomePanelsEnum) {
		this.welcomePanelsEnum = welcomePanelsEnum;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

}
