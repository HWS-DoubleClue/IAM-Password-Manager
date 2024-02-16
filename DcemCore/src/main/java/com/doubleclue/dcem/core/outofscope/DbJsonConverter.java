package com.doubleclue.dcem.core.outofscope;

import java.io.Serializable;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


@Converter (autoApply=false)
public class DbJsonConverter implements AttributeConverter<Serializable, String> {
	

	private final static ObjectMapper objectMapper = new ObjectMapper();
	private static final Logger logger = LogManager.getLogger(DbJsonConverter.class);

	
	@Override
	public String convertToDatabaseColumn(Serializable object) {
		if (object == null) {
			return null;
		}
//		java.io.StringWriter sw = new StringWriter();
//		sw.write(object.getClass().getName());
//		sw.write("\n");
//		sw.write(gson.toJson(object));
//		return sw.toString();
		
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.warn("Couldn't serialize " + object.getClass().getName(), e);
			return null;
		}
		
	}

	@Override
	public Serializable convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
//		java.io.StringReader sr = new StringReader(dbData);
//		BufferedReader reader = new BufferedReader(sr);
//		String className;
		try {
//			className = reader.readLine();
			return objectMapper.readValue(dbData, Serializable.class);
		} catch (Exception  e) {
			logger.error("Couldn't deserialize " + dbData, e);
		}
		return null;		
	}



	


}
