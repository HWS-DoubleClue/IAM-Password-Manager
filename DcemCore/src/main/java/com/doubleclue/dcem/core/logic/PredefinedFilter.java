package com.doubleclue.dcem.core.logic;

import java.util.List;
import java.util.ResourceBundle;

import javax.persistence.EntityManager;

public abstract class PredefinedFilter {
	
	ResourceBundle resourceBundle;
	
	static final String FILTER = "Filter";
		
	abstract public String getName();
	abstract public Long executeCount(EntityManager em) throws Exception;
	abstract public List<?> execute(EntityManager em, int first, int pageSize) throws Exception;

		

	abstract public String toString();

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}
		
}
