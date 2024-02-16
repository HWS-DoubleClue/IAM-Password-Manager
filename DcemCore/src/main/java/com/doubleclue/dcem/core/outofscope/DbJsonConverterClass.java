package com.doubleclue.dcem.core.outofscope;

import java.io.BufferedReader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
@Converter(autoApply = false)
public class DbJsonConverterClass implements AttributeConverter<Object, String>, Serializable {

	// private final static ObjectMapper objectMapper = new
	// ObjectMapper().configure(SerializationFeature.WRAP_ROOT_VALUE,
	// true).configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
	private final static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private static final Logger logger = LogManager.getLogger(DbJsonConverterClass.class);

	@Override
	public String convertToDatabaseColumn(Object object) {
		if (object == null) {
			return null;
		}
		try {
			java.io.StringWriter sw = new StringWriter();
			sw.write(object.getClass().getName());
			sw.write("\n");
			sw.write(objectMapper.writeValueAsString(object));
			return sw.toString();
		} catch (JsonProcessingException e) {
			logger.warn("Couldn't serialize " + object.getClass().getName(), e);
			return null;
		}
	}

	@Override
	public Object convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.isEmpty()) {
			return null;
		}
		java.io.StringReader sr = new StringReader(dbData);
		BufferedReader reader = new BufferedReader(sr);
		String className = null;
		try {
			className = reader.readLine();
			Class<?> klass = Class.forName(className);
			return objectMapper.readValue(reader.readLine(), klass);
		} catch (Exception e) {
			logger.error("Couldn't deserialize " + className, e);
		}
		return null;
	}
}
