package com.doubleclue.dcem.core.jpa;

import java.util.ArrayList;

import javax.persistence.metamodel.SingularAttribute;

public class FilterProperty {
		
	ArrayList<SingularAttribute<?, ?>> attributes;
	Object value;
	Object toValue;
	
	VariableType variableType;
	
	FilterOperator filterOperator;
	
	public FilterProperty(ArrayList<SingularAttribute<?, ?>> attributes, Object value, Object toValue, VariableType variableType, FilterOperator filterOperator	) {
		super();
		this.attributes = attributes;
		this.value = value;
		this.toValue = toValue;
		this.variableType = variableType;
		this.filterOperator = filterOperator;
	}	
	
	public FilterOperator getFilterOperator() {
		return filterOperator;
	}
	public void setFilterOperator(FilterOperator filterOperator) {
		this.filterOperator = filterOperator;
	}

	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
//	public VariableType getVariableType() {
//		return variableType;
//	}
//	public void setVariableType(VariableType variableType) {
//		this.variableType = variableType;
//	}

	public ArrayList<SingularAttribute<?, ?>> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<SingularAttribute<?, ?>> attributes) {
		this.attributes = attributes;
	}
	
	
	public Object getToValue() {
		return toValue;
	}

	public void setToValue(Object toValue) {
		this.toValue = toValue;
	}

	@Override
	public String toString() {
		return "FilterProperty [value=" + value + ", variableType=" + variableType + ", filterOperator=" + filterOperator + "]";
	}
	
	
}
