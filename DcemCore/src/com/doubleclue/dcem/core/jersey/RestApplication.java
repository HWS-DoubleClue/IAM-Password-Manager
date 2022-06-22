package com.doubleclue.dcem.core.jersey;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class RestApplication extends ResourceConfig {

	public RestApplication() {
		
		super (
				JacksonFeature.class,
				JacksonConfig.class
				);
		
		
		
//		JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
		
//		register(RolesAllowedDynamicFeature.class);
		register(JacksonFeature.class);
		register(JacksonConfig.class); 
		
		
	}
}
