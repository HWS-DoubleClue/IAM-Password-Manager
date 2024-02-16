package com.doubleclue.dcem.radius.attribute;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * This class represents a Radius attribute which only
 * contains a string.
 */
public class StringAttribute extends RadiusAttribute {

	/**
	 * Constructs an empty string attribute.
	 */
	public StringAttribute() {
		super();
	}

	/**
	 * Constructs a string attribute with the given value.
	 * @param type attribute type
	 * @param value attribute value
	 */
	public StringAttribute(int type, String value) {
		setAttributeType(type);
		setAttributeValue(value);
	}

	/**
	 * Returns the string value of this attribute.
	 * @return a string
	 */
	public String getAttributeValue(Charset charset) {
		return new String(getAttributeData(), charset);
	}

	/**
	 * Sets the string value of this attribute.
	 * @param value string, not null
	 */
	public void setAttributeValue(String value, Charset charset) {
		if (value == null) {
			throw new NullPointerException("string value not set");
		}
		setAttributeData(value.getBytes(charset));
	}

}
