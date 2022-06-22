package com.doubleclue.dcem.core;

public class SasPermission {

	String moduleId;
	String category;
	String action;
	
	
	
	

	public SasPermission(String moduleId, String category, String action) {
		super();
		this.moduleId = moduleId;
		this.category = category;
		this.action = action;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		int hash = 17;

		hash = hash * prime + ((moduleId == null) ? 0 : moduleId.hashCode());
		hash = hash * prime + ((category == null) ? 0 : category.hashCode());
		hash = hash * prime + ((action == null) ? 0 : action.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}

		SasPermission other = (SasPermission) obj;
		if (moduleId == null && other.moduleId != null) {
			return false;
		} else if (moduleId.equals(other.moduleId) == false) {
			return false;
		}

		if (category == null && other.category != null) {
			return false;
		} else if (category.equals(other.category) == false) {
			return false;
		}

		if (action == null && other.action != null) {
			return false;
		} else if (action.equals(other.action) == false) {
			return false;
		}

		return true;
	}

	public String toString() {
		return moduleId + '$' + (category == null ? "" : category) + '$' + (action == null ? "" : action);
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
