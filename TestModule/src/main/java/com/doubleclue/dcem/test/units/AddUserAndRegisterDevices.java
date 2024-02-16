package com.doubleclue.dcem.test.units;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.model.AsApiActivationCode;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.doubleclue.utils.RandomUtils;

@ApplicationScoped
@Named("AddUserAndRegisterDevices")
public class AddUserAndRegisterDevices extends AbstractTestUnit {

	private final static String DEVICE_NAME = "testDevice";

	@Inject
	AddUserWithActivationTest addUserWithActivationTest;

	@Override
	public String getDescription() {
		return "This unit will register 10 Devices;";
	}

	@Override
	public String getAuthor() {
		return "Oliver Jahn";
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new LinkedList<String>();
		dependencies.add(AddUserWithActivationTest.class.getSimpleName());
		return dependencies;
	}
	
	@Override
	public TestUnitGroupEnum getParent() {
		return TestUnitGroupEnum.ADD_USER;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	public String start() throws Exception {
		initializationSdk();
		appSdkImplSync.initialize(asVersion, appSdkListnerImplSync, appProperties, sdkConfigDcem);
		AsClientRestApi clientRestApi = getClientRestApi();
		String loginId = addUserWithActivationTest.getUser();
		String password = addUserWithActivationTest.getInitialPassword();
		for (int i = 0; i < 10; i++) {
			String deviceName = DEVICE_NAME + i;
			setInfo("activating " + deviceName + " for user " + loginId);
			AsApiActivationCode apiActivationCode = new AsApiActivationCode();
			apiActivationCode.setUserLoginId(loginId);
			apiActivationCode.setActivationCode(RandomUtils.generateRandomAlphaNumericString(4));
			String activationCode = clientRestApi.addActivationCode(apiActivationCode);
			appSdkImplSync.activation(loginId, password, activationCode, deviceName);
		}
		setInfo("OK");
		return null;
	}
}