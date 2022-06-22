package com.doubleclue.dcem.test.units;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.model.DcemApiException;
import com.doubleclue.as.restapi.model.AsApiUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;

@ApplicationScoped
@Named("AddTenUsersTest")
public class AddTenUsersTest extends AbstractTestUnit {

	public static final String USER_PREFIX = "test";

	private List<AsApiUser> users = null;

	@Inject
	TestModule testModule;

	@Override
	public String getDescription() {
		return "This unit will add 10 users";
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

	@Override
	public String start() throws Exception {
		AsClientRestApi clientRestApi = getClientRestApi();
		users = new ArrayList<>();
		int addedUserCount = 0;
		for (int i = 0; i < 10; i++) {
			AsApiUser apiUser = new AsApiUser();
			apiUser.setLoginId(USER_PREFIX + i);
			apiUser.setEmail("testmail" + i + "@hws.de");
			apiUser.setInitialPassword("abcd");
			apiUser.setDisplayName("Test desc");
			users.add(apiUser);
			String initialPassword = null;
			try {
				initialPassword = clientRestApi.addUser(apiUser);
				addedUserCount++;
			} catch (DcemApiException e) {
				if (e.getCode() != DcemErrorCodes.USER_EXISTS_ALREADY.getErrorCode()) {
					throw e;
				}
				continue;
			}
			if (initialPassword == null) {
				throw new Exception("No initial password returned");
			}
		}
		setInfo("Users added: " + addedUserCount);
		return null;
	}

	public List<AsApiUser> getUsers() {
		return users;
	}

	public AsApiUser getUser(int index) {
		return users.get(index);
	}
}
