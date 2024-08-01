package com.doubleclue.dcem.core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class EntityInterface   {

	/**
	 * 
	 */
	public abstract Number getId();

	public abstract void setId(Number id);

	@JsonIgnore
	public String getRowStyle() {
		return null;
	}
	
	@JsonIgnore
	public boolean isRestricted() {
		return false;
	}

}
