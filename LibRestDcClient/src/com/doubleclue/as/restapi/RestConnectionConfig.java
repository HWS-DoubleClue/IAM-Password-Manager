package com.doubleclue.as.restapi;

public class RestConnectionConfig {
	
	String restApiUrl = "http://localhost:8001/dcem/restApi/as";
	String operatorName = "restservicesoperator";
	String password = "password";
	int connectionTimeout = 5000;
	boolean debug = false;
	
	public String getRestApiUrl() {
		return restApiUrl;
	}
	public void setRestApiUrl(String restApiUrl) {
		this.restApiUrl = restApiUrl;
	}
	public String getOperatorName() {
		return operatorName;
	}
	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}	

}
