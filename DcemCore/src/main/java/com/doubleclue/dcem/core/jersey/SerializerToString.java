package com.doubleclue.dcem.core.jersey;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class SerializerToString extends JsonSerializer<Object> {

	@Override
	public void serialize(Object object, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeString(object.toString());
	}

}
