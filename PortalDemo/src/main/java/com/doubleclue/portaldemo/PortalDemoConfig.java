package com.doubleclue.portaldemo;

import org.primefaces.shaded.json.JSONPropertyIgnore;

import com.doubleclue.as.restapi.RestConnectionConfig;

public class PortalDemoConfig {
	
	String cookieRoute = "";
	String restApiUrl = "http://localhost:8001/dcem/restApi/as";
	String restOperatorName = "RestServicesOperator";
	String restPassword = "password";
	int restConnectionTimeout = 5000;
	boolean debug = false;
	String radiusHost = "127.0.0.1";
	int radiusPort = 1812;
	String radiusSharedSecret = "PortalDemo";
	int radiusTimeout = 20000;  
	
	public String getRestApiUrl() {
		return restApiUrl;
	}
	public void setRestApiUrl(String restApiUrl) {
		this.restApiUrl = restApiUrl;
	}
	
	
	public String getRadiusHost() {
		return radiusHost;
	}
	public void setRadiusHost(String radiusHost) {
		this.radiusHost = radiusHost;
	}
	public String getRadiusSharedSecret() {
		return radiusSharedSecret;
	}
	public void setRadiusSharedSecret(String radiusSharedSecret) {
		this.radiusSharedSecret = radiusSharedSecret;
	}
	public int getRadiusTimeout() {
		return radiusTimeout;
	}
	public void setRadiusTimeout(int radiusTimeout) {
		this.radiusTimeout = radiusTimeout;
	}
	public String getRestOperatorName() {
		return restOperatorName;
	}
	public void setRestOperatorName(String restOperatorName) {
		this.restOperatorName = restOperatorName;
	}
	public String getRestPassword() {
		return restPassword;
	}
	public void setRestPassword(String restPassword) {
		this.restPassword = restPassword;
	}
	public int getRestConnectionTimeout() {
		return restConnectionTimeout;
	}
	public void setRestConnectionTimeout(int restConnectionTimeout) {
		this.restConnectionTimeout = restConnectionTimeout;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public String getCookieRoute() {
		return cookieRoute;
	}
	public void setCookieRoute(String cookieRoute) {
		this.cookieRoute = cookieRoute;
	}
	
	public int getRadiusPort() {
		return radiusPort;
	}
	public void setRadiusPort(int radiusPort) {
		this.radiusPort = radiusPort;
	}
	
	@JSONPropertyIgnore
	public RestConnectionConfig getConnectionConfig() {
		RestConnectionConfig connectionConfig = new RestConnectionConfig();
		connectionConfig.setConnectionTimeout(getRestConnectionTimeout());
		connectionConfig.setRestApiUrl(restApiUrl);
		connectionConfig.setPassword(getRestPassword());
		connectionConfig.setOperatorName(getRestOperatorName());
		return connectionConfig;
	}
	

}
