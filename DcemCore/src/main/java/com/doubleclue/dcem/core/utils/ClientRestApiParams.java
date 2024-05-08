package com.doubleclue.dcem.core.utils;

import java.util.ArrayList;
import java.util.Base64;

import org.apache.http.auth.UsernamePasswordCredentials;

import com.doubleclue.dcem.core.utils.ClientRestApi.HttpVerb;

public class ClientRestApiParams {

	String url;
	HttpVerb httpVerb;
	UsernamePasswordCredentials credentials; // in case authHeader is null
	String authHeader = null;
	String body;
	String contentType = "application/json";
	boolean unsecure = false;
	private int timeoutSeconds = 10;
	private int requestTimeoutSeconds = 0;
	ArrayList<KeyValuePair> headers;
	boolean withoutUserAgent;
	boolean closeConnection = false;
	long responseTime;
	
		
	public ClientRestApiParams(String url, HttpVerb httpVerb, UsernamePasswordCredentials credentials, String body, String contentType) {
		super();
		this.url = url;
		this.httpVerb = httpVerb;
		this.credentials = credentials;
		this.body = body;
		this.contentType = contentType;
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


	public void setTimeoutSeconds(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}


	public int getRequestTimeout () {
		if (requestTimeoutSeconds == 0) {
			return (timeoutSeconds + 4) * 1000;
		}
		return (requestTimeoutSeconds) * 1000;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public HttpVerb getHttpVerb() {
		return httpVerb;
	}


	public void setHttpVerb(HttpVerb httpVerb) {
		this.httpVerb = httpVerb;
	}


	public UsernamePasswordCredentials getCredentials() {
		return credentials;
	}


	public void setCredentials(UsernamePasswordCredentials credentials) {
		this.credentials = credentials;
	}


	public String getBody() {
		return body;
	}


	public void setBody(String body) {
		this.body = body;
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
	
	
	
}
