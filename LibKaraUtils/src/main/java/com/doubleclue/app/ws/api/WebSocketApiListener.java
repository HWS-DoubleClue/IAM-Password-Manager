package com.doubleclue.app.ws.api;

public interface WebSocketApiListener {
	
	void onReceive (byte [] packet );
	
	void onCloseConnection (String reason);
	
//	ByteArrayInputStream waitForReceive() throws InterruptedException;

}
