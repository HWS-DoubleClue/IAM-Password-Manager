package com.doubleclue.dcem.core.utils;

import java.io.Serializable;
import java.lang.reflect.Field;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author Emanuel Galea
 */
public class SettingField implements Serializable {

	private static final long serialVersionUID = 4725468617809734809L;

	private static Logger logger = LogManager.getLogger(SettingField.class);

	private Field field;
	private String name;
	private Object value;
	private String errorMessage;

	public SettingField () {
	}	

	public SettingField(Field field, Object object) {

		this.name = field.getName();
		try {
			field.setAccessible(true);
			this.value = field.get(object);
		} catch (IllegalArgumentException e) {
			logger.warn("Failed to access content: " + this.name + ", " + object.getClass(), e);
		} catch (IllegalAccessException e) {
			logger.warn("Failed to access content: " + this.name + ", " + object.getClass(), e);
		}

		this.field = field;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name; 
	}
	
	/**
	 * put first letter as capital letter
	 * @return
	 */
	public String getDisplayName() { 
		return name.substring(0,1).toUpperCase() + name.substring(1, name.length());
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the value
	 */
	public Object getValue() {
		
		return value;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}
}
