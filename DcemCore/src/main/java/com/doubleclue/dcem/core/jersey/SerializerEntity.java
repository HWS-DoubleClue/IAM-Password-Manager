package com.doubleclue.dcem.core.jersey;

import java.io.IOException;
import java.util.Collection;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class SerializerEntity extends JsonSerializer<EntityInterface> {

	@Override
	public void serialize(EntityInterface entityInterface, JsonGenerator gen, SerializerProvider serializers) throws IOException {
		gen.writeNumber(entityInterface.getId().intValue());
	}
}
