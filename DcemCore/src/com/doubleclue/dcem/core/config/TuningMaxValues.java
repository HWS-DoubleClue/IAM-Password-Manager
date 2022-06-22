package com.doubleclue.dcem.core.config;

import java.util.HashMap;

public class TuningMaxValues {

	public final static String CAT_TOMCAT_CONNECTION = "webConnection";

	public final static String MGT_CONN_MAX_THREAD = "MgtConnectionMaxThreads";
	public final static String WS_CONN_MAX_THREAD = "WsConnectionMaxThreads";
	public final static String REST_API_CONN_MAX_THREAD = "RestApiConnectionMaxThreads";
	
	public final static String MGT_CONN_MIN_THREAD = "MgtConnectionMinThreads";
	public final static String WS_CONN_MIN_THREAD = "WsConnectionMinThreads";
	public final static String REST_API_CONN_MIN_THREAD = "RestApiConnectionMinThreads";
	
	public final static String WS_MAX_CONNECTIONS = "WsMaxConnections";


	static HashMap<String, TuningValue> map;

	static public void createDefaults() {
		map = new HashMap<>();
		addValue(MGT_CONN_MAX_THREAD, 200,
				"The maximum number of request processing threads to be created by this Managemtn-Connector. Determines the maximum number of simultaneous requests.");
				
		addValue(REST_API_CONN_MAX_THREAD, 200,
				"The maximum number of request processing threads to be created by this REST-API Connector. Determines the maximum number of simultaneous requests.");

		addValue(WS_CONN_MAX_THREAD, 600,
				"The maximum number of request processing threads to be created by this Web-Sockets Client Connection, determines the maximum number of simultaneous requests");

		addValue(MGT_CONN_MIN_THREAD, 10,
				"The number of request processing threads that will be created when this Connector is first started");
				
		addValue(REST_API_CONN_MIN_THREAD, 10,
				"The number of request processing threads that will be created when this Connector is first started");

		addValue(WS_CONN_MIN_THREAD, 20,
				"The number of request processing threads that will be created when this Connector is first started");

	}

	static public void addValue(String name, int maxValue, String description) {
		map.put(name, new TuningValue(name, maxValue, description));

	}
	
	static public int getMaxValueOf(String name, int scaleFactor) throws Exception {
		if (map == null) {
			throw new Exception ("Values are not yet initialized");
		}
		TuningValue tuningValue = map.get(name);
		if (tuningValue == null) {
			throw new Exception ("Value not found");
		}
		
		return (tuningValue.getMaxvalue() / 100) * scaleFactor;
		
	}

}
