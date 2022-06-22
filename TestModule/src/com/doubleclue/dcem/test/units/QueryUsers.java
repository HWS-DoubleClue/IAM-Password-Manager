package com.doubleclue.dcem.test.units;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.RestConnectionConfig;
import com.doubleclue.as.restapi.model.AsApiFilterItem;
import com.doubleclue.as.restapi.model.AsApiFilterItem.OperatorEnum;
import com.doubleclue.as.restapi.model.AsApiFilterItem.SortOrderEnum;
import com.doubleclue.as.restapi.model.AsApiUser;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Named("QueryUsers")
public class QueryUsers extends AbstractTestUnit {

	@Inject
	TestModule testModule;

	@Override
	public String getDescription() {
		return "This unit test the REST getUser and queryUsers methods";
	}

	@Override
	public String getAuthor() {
		return "Emanuel Galea";
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new LinkedList<String>();
		dependencies.add(AddTenUsersTest.class.getSimpleName());
		return dependencies;
	}	
	
	@Override
	public TestUnitGroupEnum getParent() {
		return TestUnitGroupEnum.REST;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	@Override
	public String start() throws Exception {

		System.out.println("RestEcho.start()");
		RestConnectionConfig restConnectionConfig = new RestConnectionConfig();
		ObjectMapper objectMapper = new ObjectMapper();
		restConnectionConfig = objectMapper.readValue(testModule.getPreferences().getRestConnectionConfig(), RestConnectionConfig.class);
		AsClientRestApi clientRestApi = AsClientRestApi.initilize(restConnectionConfig);

		String loginId = AddTenUsersTest.USER_PREFIX + "0";
		AsApiUser user = clientRestApi.getUser(loginId);
		if (user == null || user.getLoginId().equals(loginId) == false) {
			throw new Exception("getUser failed");
		}

		List<AsApiFilterItem> filters = new LinkedList<>();
		filters.add(new AsApiFilterItem("loginId", AddTenUsersTest.USER_PREFIX + "%", SortOrderEnum.ASCENDING, OperatorEnum.EQUALS));
		List<AsApiUser> users = clientRestApi.queryUsers(filters, 0, 100);

		StringBuffer sb = new StringBuffer();
		for (AsApiUser user2 : users) {
			sb.append(user2.getLoginId());
			sb.append(", ");
		}
		setInfo("Users found with queryUsers: " + sb.toString());

		return null;
	}

}
