package com.doubleclue.dcem.as.logic;

public class CloudSafeNameDto {

	private int id;
	private String name;
	private Integer parentId;
	
	public CloudSafeNameDto() {
		super();
	}
	
	public CloudSafeNameDto(int id, String name, Integer parentId) {
		super();
		this.id = id;
		this.name = name;
		this.parentId = parentId;	
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getParentId() {
		return parentId;
	}
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
}
