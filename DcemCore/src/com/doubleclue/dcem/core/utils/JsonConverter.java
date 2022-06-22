package com.doubleclue.dcem.core.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {

	private final static ObjectMapper objectMapper = new ObjectMapper();

	static public  HashMap<String, String> getAsMap(String json) throws JsonParseException, JsonMappingException, IOException {
		TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
		};
		HashMap<String, String> result = objectMapper.readValue(json, typeRef);
		return result;
	}
	
	static public  String getMapToString(Map<String, String> map) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.writeValueAsString(map);
	}
	
	static public  List<String> getAsList(String json) throws JsonParseException, JsonMappingException, IOException {
		TypeReference<List<String>> typeRef = new TypeReference<List<String>>() {
		};
		List<String> result = objectMapper.readValue(json, typeRef);
		return result;
	}
	
	static public  Set<String> getAsSet(String json) throws JsonParseException, JsonMappingException, IOException {
		TypeReference<Set<String>> typeRef = new TypeReference<Set<String>>() {
		};
		Set<String> result = objectMapper.readValue(json, typeRef);
		return result;
	}
	
	static public  String serializeObject (Object object) throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.writeValueAsString(object);
	}

}
