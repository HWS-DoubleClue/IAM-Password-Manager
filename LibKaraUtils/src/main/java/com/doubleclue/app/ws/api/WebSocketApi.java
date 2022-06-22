package com.doubleclue.app.ws.api;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public interface WebSocketApi {

	public static final String DEFAULT_KEYSTORE_PASSWORD = "abcd";

	void init(WebSocketApiListener listener, URI Url, List<byte[]> certList, String[] supportedCiphers, int connectionTimeout) throws Exception;

	void setProxy(boolean proxyEnabled, String proxyAdress, int proxyPort);

	WebSocketApiListener getListener();

	void connect() throws Exception, Throwable;

	public void send(byte[] buffer) throws IOException, Exception;

	public void close(String reason) throws IOException;

	public boolean isOpen();
}
