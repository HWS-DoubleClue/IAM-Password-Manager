package com.doubleclue.dcem.core.jersey;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JacksonConfig implements ContextResolver<ObjectMapper> {

	private ObjectMapper mapper = new ObjectMapper();

	public JacksonConfig() {

		mapper = new ObjectMapper();	/* Register JodaModule to handle Joda DateTime Objects. */
		// mapper.registerModule(new JodaModule());
		// /* We want dates to be treated as ISO8601 not timestamps. */
		// mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
		mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
//		DateFormat dateFormat = new SimpleDateFormat(DcemConstants.DAY_TIME_FORMAT);
		// DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//		mapper.setDateFormat((DateFormat) dateFormat.clone());
		// mapper.configure(Feature.INDENT_OUTPUT, true);
		// mapper.enable(SerializationConfig.Feature.INDENT_OUTPUT);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.registerModule(new JavaTimeModule());
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return mapper;
	}

}
