package com.doubleclue.dcem.core.outofscope;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.gui.UserAccount;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@Converter (autoApply=false)
public class DbJsonListConverter implements AttributeConverter<LinkedList<?>, String>, Serializable {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final static ObjectMapper objectMapper = new ObjectMapper();
	
	private static final Logger logger = LogManager.getLogger(DbJsonListConverter.class);
	
	@Override
	public String convertToDatabaseColumn(LinkedList<?> object) {
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
			// TODO Auto-generated catch block
			logger.error("Couldn't deserialize " + object.toString(), e);
			return null;
		}
		
	}

	@Override
	public LinkedList<?> convertToEntityAttribute(String dbData) {
		if (dbData == null) {
			return null;
		}
//		java.io.StringReader sr = new StringReader(dbData);
//		BufferedReader reader = new BufferedReader(sr);
//		String className;
//		TypeReference<List<LinkedList<?>>> typeRef = new TypeReference<List<LinkedList<?>>>() {
//		};
		try {
			return objectMapper.readValue(dbData, LinkedList.class);
		} catch (Exception  e) {
			logger.warn("Couldn't deserialize " + dbData, e);
		}
		return null;
		
	}



	


}
