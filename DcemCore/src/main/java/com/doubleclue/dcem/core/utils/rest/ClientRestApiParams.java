package com.doubleclue.dcem.core.utils.rest;

import java.util.ArrayList;
import java.util.Base64;

import org.apache.http.auth.UsernamePasswordCredentials;

import com.doubleclue.dcem.core.utils.KeyValuePair;

public class ClientRestApiParams {

	String url;
	UsernamePasswordCredentials credentials; // in case authHeader is null
	String authHeader = null;
	String contentType = "application/json";
	boolean unsecure = false;
	private int timeoutSeconds = 10;
	private int requestTimeoutSeconds = 0;
	ArrayList<KeyValuePair> headers;
	boolean withoutUserAgent;
	boolean closeConnection = false;
	long responseTime;
	String responseBody;
	int responseCode;	
	
	public ClientRestApiParams(String url) {
		super();
		this.url = url;
	}
	
	public ClientRestApiParams(String url, String authHeader) {
		super();
		this.url = url;
		this.authHeader = authHeader;
	}
		
	public ClientRestApiParams(String url, UsernamePasswordCredentials credentials) {
		super();
		this.url = url;
		this.credentials = credentials;
	}	
	
	public ClientRestApiParams(String url, UsernamePasswordCredentials credentials, int requestTimeoutSeconds ) {
		super();
		this.url = url;
		this.credentials = credentials;
		this.requestTimeoutSeconds = requestTimeoutSeconds;
	}
	
	public ClientRestApiParams(String url, UsernamePasswordCredentials credentials, boolean unsecure, int requestTimeoutSeconds) {
		super();
		this.url = url;
		this.credentials = credentials;
		this.unsecure = unsecure;
		this.requestTimeoutSeconds = requestTimeoutSeconds;
	}


	public String getAuthHeader () {
		if (authHeader != null) {
			return authHeader;
		}
		if (credentials != null) {
			authHeader = "Basic " + new String(Base64.getEncoder().encodeToString((credentials.getUserName() + ":" + credentials.getPassword()).getBytes()));
		}
		return authHeader;
	}


	public int getTimeoutMilli() {
		return timeoutSeconds * 1000;
	}


//	public void setTimeoutSeconds(int timeoutSeconds) {
//		this.timeoutSeconds = timeoutSeconds;
//	}
	
	public ClientRestApiParams setTimeoutSeconds(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
		return this;
	}


	public int getRequestTimeout () {
		if (requestTimeoutSeconds == 0) {
			return (timeoutSeconds * 2) * 1000;
		}
		return (requestTimeoutSeconds) * 1000;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public UsernamePasswordCredentials getCredentials() {
		return credentials;
	}


	public void setCredentials(UsernamePasswordCredentials credentials) {
		this.credentials = credentials;
	}

	public String getContentType() {
		return contentType;
	}


	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public boolean isUnsecure() {
		return unsecure;
	}

	public void setUnsecure(boolean unsecure) {
		this.unsecure = unsecure;
	}

	public int getRequestTimeoutSeconds() {
		return requestTimeoutSeconds;
	}

	public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
		this.requestTimeoutSeconds = requestTimeoutSeconds;
	}

	public ArrayList<KeyValuePair> getHeaders() {
		return headers;
	}

	public void setHeaders(ArrayList<KeyValuePair> headers) {
		this.headers = headers;
	}

	public boolean isWithoutUserAgent() {
		return withoutUserAgent;
	}

	public void setWithoutUserAgent(boolean withoutUserAgent) {
		this.withoutUserAgent = withoutUserAgent;
	}

	public boolean isCloseConnection() {
		return closeConnection;
	}
	public void setCloseConnection(boolean closeConnection) {
		this.closeConnection = closeConnection;
	}
	public int getTimeoutSeconds() {
		return timeoutSeconds;
	}
	public void setAuthHeader(String authHeader) {
		this.authHeader = authHeader;
	}
	public long getResponseTime() {
		return responseTime;
	}
	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}
	public String getResponseBody() {
		return responseBody;
	}
	public void setResponseBody(String responseBody) {
		this.responseBody = responseBody;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
			
}
