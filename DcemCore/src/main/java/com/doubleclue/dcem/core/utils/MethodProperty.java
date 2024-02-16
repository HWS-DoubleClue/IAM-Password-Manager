package com.doubleclue.dcem.core.utils;

import java.lang.reflect.Method;

import javax.faces.convert.Converter;

public class MethodProperty {
	
	public enum MethodPropertyType {
		LIST, SORTEDLIST, OTHER;
	}

	String name;
	Method method;
	Converter converter;
	Object klassObject = null;
	MethodPropertyType objectType;

	public MethodProperty(String name) {
		this.name = name;
	}

	public MethodProperty(String name, Method method) {
		this.name = name;
		this.method = method;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		if (method == null) {
			System.out.println("MethodProperty.setMethod()");
		}
		this.method = method;
	}

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	public Object getKlassObject() {
		return klassObject;
	}

	public void setKlassObject(Object klassObject) {
		this.klassObject = klassObject;
	}

	@Override
	public String toString() {
		return "MethodProperty [name=" + name + ", method=" + method + ", converter=" + converter + ", klassObject=" + klassObject + "]";
	}

	public MethodPropertyType getObjectType() {
		return objectType;
	}

	public void setObjectType(MethodPropertyType objectType) {
		this.objectType = objectType;
	}

}
