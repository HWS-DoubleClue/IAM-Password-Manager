package com.doubleclue.dcem.test.units;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.doubleclue.sdk.api.Account;
import com.doubleclue.sdk.api.AppLoginResponse;
import com.doubleclue.sdk.api.AsException;

@ApplicationScoped
@Named("ReLoginTest")
public class ReLoginTest extends AbstractTestUnit {

	final static String DEVICE_NAME = "testDevice";

	@Inject
	TestModule testModule;

	@Inject
	AddUserWithActivationTest userWithActivationTest;

	@Inject
	ActivateLoginTest activateLoginTest;

	@Override
	public String getDescription() {
		return "This unit will test the reLogin using the sessionCookie";
	}

	@Override
	public String getAuthor() {
		return "Emanuel Galea";
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new LinkedList<String>();
		dependencies.add(AddUserWithActivationTest.class.getSimpleName());
		dependencies.add(ActivateLoginTest.class.getSimpleName());
		return dependencies;
	}	
	
	@Override
	public TestUnitGroupEnum getParent() {
		return TestUnitGroupEnum.LOGIN_AND_ACTIVATION;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	@Override
	public String start() throws Exception {

		appSdkImplSync.logoff(null);

//		String sessionCookie = activateLoginTest.getAppLoginResponse().getSessionCookie();
//		if (sessionCookie == null || sessionCookie.length() < 32) {
//			throw new Exception("Invalid sessioncookie");
//		}
//
//		AppLoginResponse appLoginResponse = appSdkImplSync.reloginAccount(activateLoginTest.getAccount().getName(), sessionCookie);
//		Date date = new Date((long)appLoginResponse.getSessionCookieExpiresOn() * 1000);
//		setInfo("SessionCookie Valid till: " + date);
//		activateLoginTest.setAppLoginResponse(appLoginResponse);
//		Date updateTill = appLoginResponse.getUpdateTill();
//		if (updateTill != null) {
//			addInfo("login=OK, Update Version:" + updateTill.toString());
//		}
		return null;
	}

}
