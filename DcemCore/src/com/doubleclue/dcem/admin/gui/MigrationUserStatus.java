package com.doubleclue.dcem.admin.gui;

public class MigrationUserStatus {
	String name;
	String status;
	
		
	public MigrationUserStatus(String name, String status) {
		super();
		this.name = name;
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "MigrationUserStatus [name=" + name + ", status=" + status + "]";
	}
	
	
}
