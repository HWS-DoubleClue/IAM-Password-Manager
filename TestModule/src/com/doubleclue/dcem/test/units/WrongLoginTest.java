package com.doubleclue.dcem.test.units;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.model.AsApiUser;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.doubleclue.sdk.api.AsErrorCodes;
import com.doubleclue.sdk.api.AsException;

@ApplicationScoped
@Named("WrongLoginTest")
public class WrongLoginTest extends AbstractTestUnit {

	final static String DEVICE_NAME = "testDevice";

	@Inject
	TestModule testModule;

	@Inject
	AddUserWithActivationTest userWithActivationTest;

	@Override
	public String getDescription() {
		return "Wrong password and users for activation, login and changePassword";
	}

	@Override
	public String getAuthor() {
		return "Emanuel Galea";
	}

	String deviceName;

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
		initializationSdk();
		AsClientRestApi clientRestApi = getClientRestApi();
		appSdkImplSync.initialize(asVersion, appSdkListnerImplSync, appProperties, sdkConfigDcem);
		deviceName = DEVICE_NAME;

		/*
		 *  Wrong activation with wrong Password
		 */
		try {
			appSdkImplSync.activation(userWithActivationTest.getUser(), "XXXXXXXXXX", userWithActivationTest.getActivationCode(), deviceName);
			throw new Exception ("Wrong Activation didn't failed");
		} catch (AsException exp) {

		}

		/*
		 *  Wrong login with wrong user
		 */
		try {
			appSdkImplSync.loginAccount("XXXX", "XXXX", false);
			throw new Exception("No Exception for wrong userLogin");
		} catch (AsException exp) {
			if (exp.getErrorCode() != AsErrorCodes.INVALID_USER) {
				throw new Exception("Error is not INVALID_USER");
			}
		}
		appSdkImplSync.logoff(null);
			
		/*
		 *  Wrong login with wrong password
		 */
		try {
			appSdkImplSync.loginAccount(userWithActivationTest.getAccount().getName(), "XXXX", false);
			
		} catch (AsException e) {
			// TODO: handle exception
		}
		

		appSdkImplSync.logoff(null);
			
		appSdkImplSync.loginAccount(userWithActivationTest.getAccount().getName(), userWithActivationTest.getInitialPassword(), false);
		

		try {
		appSdkImplSync.changePassword("XXXXX", "XXXX");
		throw new Exception("No Exception for wrong password");
		} catch (AsException e) {
			// TODO: handle exception
		}
		
		appSdkImplSync.logoff(null);
			
		/*
		 *  Wrong login with user disabled
		 */
		AsApiUser user = clientRestApi.getUser(userWithActivationTest.getUser());
		user.setDisabled(true);
		clientRestApi.modifyUser(user);
		appSdkImplSync.logoff(null);
		try {
		appSdkImplSync.loginAccount(userWithActivationTest.getAccount().getName(), userWithActivationTest.getInitialPassword(), false);
		throw new Exception("No Exception for user login disabled");
		} catch (AsException e) {
			// TODO: handle exception
		}
		

		user.setDisabled(false);
		clientRestApi.modifyUser(user);
		setInfo("OK");
		return null;
	}

	public String getUser() throws AsException {
		return userWithActivationTest.getUser();
	}

	public String getDeviceName() {
		return deviceName;
	}

}
