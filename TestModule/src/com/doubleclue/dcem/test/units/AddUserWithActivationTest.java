package com.doubleclue.dcem.test.units;

import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.model.AsApiActivationCode;
import com.doubleclue.as.restapi.model.DcemApiException;
import com.doubleclue.as.restapi.model.AsApiUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.doubleclue.sdk.api.Account;

@ApplicationScoped
@Named("AddUserWithActivationTest")
public class AddUserWithActivationTest extends AbstractTestUnit {

	final static String PASSWORD = "1234";

	@Inject
	TestModule testModule;

	@Override
	public String getDescription() {
		return "Add user if it doesn't exist and add a new activation code";
	}

	@Override
	public String getAuthor() {
		return "Emanuel Galea";
	}

	@Override
	public List<String> getDependencies() {
		return null;
	}
	
	@Override
	public TestUnitGroupEnum getParent() {
		return TestUnitGroupEnum.ADD_USER;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	String user;
	Account account;
	String initialPassword;
	String activationCode;

	@Override
	public String start() throws Exception {
		boolean userCreated = false;
		// RestConnectionConfig restConnectionConfig = new RestConnectionConfig();
		//
		// ObjectMapper objectMapper = new ObjectMapper();
		// restConnectionConfig = objectMapper.readValue(testModule.getPreferences().getRestConnectionConfig(),
		// RestConnectionConfig.class);
		// AsClientRestApi clientRestApi = AsClientRestApi.initilize(restConnectionConfig);
		AsClientRestApi clientRestApi = getClientRestApi();
		user = testModule.getPreferences().getTestUser();
		AsApiUser apiUser = null;
		try {
			apiUser = clientRestApi.getUser(user);
		} catch (DcemApiException e) {
			if (e.getCode() != DcemErrorCodes.INVALID_USERID.getErrorCode()) {
				throw e;
			}
		}
		if (apiUser == null) {
			apiUser = new AsApiUser();
			apiUser.setLoginId(user);
			apiUser.setInitialPassword(PASSWORD);
			apiUser.setPreferedLanguage(Locale.GERMAN.getLanguage());
			initialPassword = clientRestApi.addUser(apiUser);
		}
		initialPassword = PASSWORD;
		AsApiActivationCode apiActivationCode = new AsApiActivationCode();
		apiActivationCode.setUserLoginId(user);
		apiActivationCode.setActivationCode("abcd");
		activationCode = clientRestApi.addActivationCode(apiActivationCode);
		setInfo("User: " + user + ", created: " + userCreated + ", Activation: " + activationCode + ", password: " + initialPassword);
		return null;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getInitialPassword() {
		return initialPassword;
	}

	public void setInitialPassword(String initialPassword) {
		this.initialPassword = initialPassword;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public Account getAccount() {
		if (account == null) {
			account = new Account(user, user, null, null, user, user);
		}
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

}
