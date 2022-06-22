package com.doubleclue.dcem.core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class EntityInterface {

	public abstract Number getId();

	public abstract void setId(Number id);

	@JsonIgnore
	public String getRowStyle() {
		return null;
	}

}
