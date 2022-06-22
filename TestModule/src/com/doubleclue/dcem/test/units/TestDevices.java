package com.doubleclue.dcem.test.units;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.RestConnectionConfig;
import com.doubleclue.as.restapi.model.AsApiDevice;
import com.doubleclue.as.restapi.model.AsApiFilterItem;
import com.doubleclue.as.restapi.model.AsApiFilterItem.OperatorEnum;
import com.doubleclue.as.restapi.model.AsApiFilterItem.SortOrderEnum;
import com.doubleclue.as.restapi.model.AsApiOtpToken;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Named("TestDevices")
public class TestDevices extends AbstractTestUnit {

	@Inject
	TestModule testModule;

	@Override
	public String getDescription() {

		return "This unit test the Rest devices";
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

		List<AsApiFilterItem> filters = new LinkedList<>();
		filters.add(new AsApiFilterItem("user.loginId", "test", SortOrderEnum.ASCENDING, OperatorEnum.LIKE));
		List<AsApiDevice> devices = clientRestApi.queryDevices(filters, 0, 100);
		StringBuffer sb = new StringBuffer();
		for (AsApiDevice device : devices) {
			sb.append(device.getUserloginId() + '-' + device.getName());
			sb.append(", ");
			clientRestApi.deleteDevice(device.getDeviceId());
		}

		filters.clear();
		filters.add(new AsApiFilterItem("user.loginId", "eman", SortOrderEnum.ASCENDING, OperatorEnum.LIKE));
		List<AsApiOtpToken> otpTokens = clientRestApi.queryOtpTokens(filters, 0, 100);

		setInfo("Devices found: " + sb.toString());
		return null;
	}

}
