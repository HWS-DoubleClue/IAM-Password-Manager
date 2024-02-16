package com.doubleclue.dcem.test.preferences;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@XmlType
@XmlRootElement(name = "testPreferences")
public class TestPreferences extends ModulePreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@DcemGui(style = "width: 850px")
	String restConnectionConfig = "{\"restApiUrl\":\"http://localhost:8001/dcem/restApi/as\",\"operatorName\":\"RestServicesOperator\",\"password\":\"test\",\"connectionTimeout\":5000,\"debug\":false}";

	@DcemGui
	String testUser = "TestUser";

	@DcemGui
	int radiusPort = 1812;

	public String getTestUser() {
		return testUser;
	}

	public void setTestUser(String testUser) {
		this.testUser = testUser;
	}

	public String getRestConnectionConfig() {
		return restConnectionConfig;
	}

	public void setRestConnectionConfig(String restConnectionConfig) {
		this.restConnectionConfig = restConnectionConfig;
	}

	public int getRadiusPort() {
		return radiusPort;
	}

	public void setRadiusPort(int radiusPort) {
		this.radiusPort = radiusPort;
	}
}
