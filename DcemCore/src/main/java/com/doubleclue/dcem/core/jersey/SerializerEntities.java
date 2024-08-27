package com.doubleclue.dcem.core.jersey;

import java.io.IOException;
import java.util.Collection;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class SerializerEntities extends JsonSerializer<Collection<EntityInterface>> {

	@Override
	public void serialize(Collection<EntityInterface> list, JsonGenerator gen, SerializerProvider serializers) throws IOException {
	//	gen.writeNumber(entity.getId().intValue());
		int [] intArray = new int [list.size()]; 
		int count = 0;
		for (EntityInterface entityInterface : list) {
			intArray[count++] = entityInterface.getId().intValue();
		}
		gen.writeArray(intArray, 0, intArray.length);
	}
}
