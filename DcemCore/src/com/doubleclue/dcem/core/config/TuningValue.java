package com.doubleclue.dcem.core.config;

public class TuningValue {
	
	String name;
	int maxvalue;
	String description;
	
	
	public TuningValue(String name, int maxvalue, String description) {
		super();
		this.name = name;
		this.maxvalue = maxvalue;
		this.description = description;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public int getMaxvalue() {
		return maxvalue;
	}


	public void setMaxvalue(int maxvalue) {
		this.maxvalue = maxvalue;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


}
