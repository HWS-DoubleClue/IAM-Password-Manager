package com.doubleclue.dcem.core.jpa;

/**
 * 
 * This enumeration contains all the allowed filter types.
 * 
 */
public enum FilterOperator {

	LIKE(true),
	EQUALS(true),
	GREATER(true),
	LESSER (true), BETWEEN(true), ISNULL (false), ISNOTNULL (false), NOT_EQUALS(true), IS_TRUE(false), IS_FALSE(false), NONE(false), LESS_NOW(false);
	
	boolean valueRequired;
	

	FilterOperator(boolean valueRequired) {
		this.valueRequired = valueRequired;
	}

	public boolean isValueRequired() {
		return valueRequired;
	}


	public void setValueRequired(boolean valueRequired) {
		this.valueRequired = valueRequired;
	}


}
