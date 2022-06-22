package com.doubleclue.dcem.core.logic;

import java.io.Serializable;

public class ClaimAttribute implements Serializable {

	private static final long serialVersionUID = 1L;

	public ClaimAttribute() {
		super();
	}

	private String name;
	private String subName;
	private AttributeTypeEnum attributeTypeEnum;
	private String value;

	public ClaimAttribute(String name, AttributeTypeEnum attributeTypeEnum, String value) {
		super();
		this.name = name;
		this.attributeTypeEnum = attributeTypeEnum;
		this.value = value;
	}

	public ClaimAttribute(ClaimAttribute ca) {
		this.name = ca.name;
		this.attributeTypeEnum = ca.attributeTypeEnum;
		this.value = ca.value;
		this.subName = ca.subName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}

	public AttributeTypeEnum getAttributeTypeEnum() {
		return attributeTypeEnum;
	}

	public void setAttributeTypeEnum(AttributeTypeEnum attributeTypeEnum) {
		this.attributeTypeEnum = attributeTypeEnum;
	}
	
	public void setUserPropertyEnum(AttributeTypeEnum attributeTypeEnum) { // migration from SAML
		this.attributeTypeEnum = attributeTypeEnum;
	}
	
	public void setProperty(AttributeTypeEnum attributeTypeEnum) { // migration from OpenID
		this.attributeTypeEnum = attributeTypeEnum;
	}

	@Override
	public String toString() {
		return "ClaimAttribute [name=" + name + ", subName=" + subName + ", attributeTypeEnum=" + attributeTypeEnum + ", value=" + value + "]";
	}
}
