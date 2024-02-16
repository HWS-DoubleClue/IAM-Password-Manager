package com.doubleclue.dcem.userportal.logic;

import java.io.Serializable;

import com.doubleclue.dcem.userportal.entities.ApplicationHubEntity;

/**
 * The persistent class for the application hub database table.
 * @author  Emanuel Galea
 */


public class MyApplication  implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private AppHubApplication application;
	private byte[] logo;
	private String name;
	
	public MyApplication() {
	}
	
	public MyApplication(ApplicationHubEntity applicationHubEntity) {
		super();
		this.name = applicationHubEntity.getName();
		this.application = applicationHubEntity.getApplication();
		this.logo = applicationHubEntity.getLogo();
	}
	
	public MyApplication(String name, AppHubApplication application, byte[] logo) {
		super();
		this.name = name;
		this.application = application;
		this.logo = logo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AppHubApplication getApplication() {
		return application;
	}

	public void setApplication(AppHubApplication application) {
		this.application = application;
	}

	public byte[] getLogo() {
		return logo;
	}

	public void setLogo(byte[] logo) {
		this.logo = logo;
	}

	@Override
	public String toString() {
		return "Name=" + name + ", application=" + application;
	}
	
}