package com.doubleclue.dcem.ps.entities;

import java.io.BufferedReader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.ps.logic.AppHubApplication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
@Converter(autoApply = false)
public class DbJsonConverterAppHub implements AttributeConverter<Object, String>, Serializable {

	// private final static ObjectMapper objectMapper = new
	// ObjectMapper().configure(SerializationFeature.WRAP_ROOT_VALUE,
	// true).configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
	private final static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	private static final Logger logger = LogManager.getLogger(DbJsonConverterAppHub.class);

	@Override
	public String convertToDatabaseColumn(Object object) {
		if (object == null) {
			return null;
		}
		try {
			java.io.StringWriter sw = new StringWriter();
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
		try {
			Class<?> klass = AppHubApplication.class;
			return objectMapper.readValue(reader.readLine(), klass);
		} catch (Exception e) {
			logger.error("Couldn't deserialize " + "AppHubApplication", e);
		}
		return null;
	}
}
