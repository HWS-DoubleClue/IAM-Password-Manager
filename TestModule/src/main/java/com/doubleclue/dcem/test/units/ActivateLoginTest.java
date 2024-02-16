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
@Named("ActivateLoginTest")
public class ActivateLoginTest extends AbstractTestUnit {

	final static String DEVICE_NAME = "testDevice";

	@Inject
	TestModule testModule;

	@Inject
	AddUserWithActivationTest userWithActivationTest;
	
	
	AppLoginResponse appLoginResponse;
	Account account;
	
	@Override
	public String getDescription() {
		return "This unit will write and read propeties using SDK and REST";
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
		appSdkImplSync.initialize(asVersion, appSdkListnerImplSync, appProperties, sdkConfigDcem);
		List<Account> accounts = appSdkImplSync.getAccounts();
		deviceName = DEVICE_NAME;
		account = getAccountbyUser(accounts, userWithActivationTest.getUser());
		if (account == null) {
			account = appSdkImplSync.activation(userWithActivationTest.getUser(), userWithActivationTest.getInitialPassword(),
					userWithActivationTest.getActivationCode(), deviceName);
			userWithActivationTest.setAccount(account);
		}
		setInfo("activation=OK");
		appLoginResponse = appSdkImplSync.loginAccount(account.getName(), userWithActivationTest.getInitialPassword(), false);
		Date updateTill = appLoginResponse.getUpdateTill();
		if (updateTill != null) {
			addInfo("login=OK, Update Version:" + updateTill.toString());
		}
		addInfo("login=OK");
		appSdkImplSync.logoff(null);
		return null;
	}

	public String getUser() throws AsException {
		return userWithActivationTest.getUser();
	}

	public String getDeviceName() {
		return deviceName;
	}
	
	private Account getAccountbyUser(List<Account> accounts, String user) {
		for (Account account : accounts) {
			if  (account.getFullQualifiedName().equals(user)) {
				return account;
			}
		}
		return null;
	}

	public AppLoginResponse getAppLoginResponse() {
		return appLoginResponse;
	}

	public void setAppLoginResponse(AppLoginResponse appLoginResponse) {
		this.appLoginResponse = appLoginResponse;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}
