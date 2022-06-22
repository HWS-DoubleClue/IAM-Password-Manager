package com.doubleclue.dcem.as.comm;


	import javax.websocket.server.ServerEndpointConfig.Configurator;

	public class EndPointConfiguration extends Configurator {
	   
	    
	    private final static AppWsConnection appWsConnection = new AppWsConnection();
	    
	    public EndPointConfiguration() {
//	    	System.out.println("EndPointConfiguration.EndPointConfiguration()");
	    }
	    
	    @SuppressWarnings("unchecked")
	    @Override
	    public <T> T getEndpointInstance(Class<T> endpointClass)
	        throws InstantiationException {
	        
	        return (T) appWsConnection;
	    }
	}


