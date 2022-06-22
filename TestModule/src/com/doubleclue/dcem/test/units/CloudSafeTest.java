package com.doubleclue.dcem.test.units;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;



import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.model.AsApiFilterItem;
import com.doubleclue.as.restapi.model.AsApiFilterItem.OperatorEnum;
import com.doubleclue.as.restapi.model.AsApiFilterItem.SortOrderEnum;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.doubleclue.sdk.api.AsCloudSafe;
import com.doubleclue.sdk.api.AsCloudSafeKey;
import com.doubleclue.sdk.api.AsCloudSafeOwner;
import com.doubleclue.utils.RandomUtils;

@ApplicationScoped
@Named("CloudSafeTest")
public class CloudSafeTest extends AbstractTestUnit {

	@Inject
	TestModule testModule;

	@Inject
	ActivateLoginTest activateLoginTest;

	private static final String FILENAME = "largeTest.kdbx";
	
	private static final byte [] FILLING = " 123456789ABCDEF".getBytes();

	@Override
	public String getDescription() {
		return "This unit test the cloudsafe files and shares";
	}

	@Override
	public String getAuthor() {
		return "Emanuel Galea";
	}

	static final String DATA_CONTENT_1 = "€abcdefghijklmnöäüß";

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new LinkedList<String>();
		dependencies.add(AddUserWithActivationTest.class.getSimpleName());
		dependencies.add(ActivateLoginTest.class.getSimpleName());
		return dependencies;
	}	
	
	@Override
	public TestUnitGroupEnum getParent() {
		return TestUnitGroupEnum.USERPORTAL;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	@Override
	public String start() throws Exception {

		AsClientRestApi clientRestApi = getClientRestApi();
		AsCloudSafeKey asCloudSafeKey = new AsCloudSafeKey(AsCloudSafeOwner.DEVICE, "hws.notification", 0, null);
		AsCloudSafe asCloudData = new AsCloudSafe(asCloudSafeKey, DATA_CONTENT_1.getBytes(DcemConstants.CHARSET_UTF8),
				"option", false, false);
		appSdkImplSync.setCloudSafe(asCloudData);
		addInfo("setCloudData from device, synchronous");
		// signed data Cloud
		byte[] content = new byte [(1024 * 64 * 4) + 16];
		
		for (int i = 0; i < content.length;) {
			System.arraycopy(FILLING, 0, content, i, FILLING.length);
			i = i + FILLING.length;
		}
		AsCloudSafeKey asCloudSafeKeyUser = new AsCloudSafeKey(AsCloudSafeOwner.USER, FILENAME, 0, null);
		AsCloudSafe asCloudData2 = new AsCloudSafe(asCloudSafeKeyUser, content, "ENC", false, false);
//		asCloudData2.setSign(true);
		try {
			appSdkImplSync.setCloudSafe(asCloudData2);
		} catch (Exception exp) {
			throw exp;
		}
		addInfo("setCloudData from user");
		asCloudData = appSdkImplSync.getCloudSafe(asCloudSafeKey);
		if (new String(asCloudData.getContent(), DcemConstants.CHARSET_UTF8).equals(DATA_CONTENT_1) == false) {
			throw new Exception("getCloudData with wrong contents");
		}
		addInfo("getCloudData OK");
		
		List<AsCloudSafe> list = appSdkImplSync.getCloudSafeList("%Signed%", 0, AsCloudSafeOwner.USER);
		System.out.println("CloudSafeTest.start()" + list.size());
		if (list.size() != 1) {
			throw new Exception("getCloudSafeList return wrong list size");
		}

		AsCloudSafe asCloudSafeUser = appSdkImplSync.getCloudSafe(asCloudSafeKeyUser);
		if (Arrays.equals(asCloudSafeUser.getContent(), content) == false) {
			throw new Exception("getCloudSafe Compare Big Files returns bad content");
		}

		List<AsApiFilterItem> filters = new LinkedList<>();
		filters.add(new AsApiFilterItem("owner", "1", SortOrderEnum.ASCENDING, OperatorEnum.EQUALS));
		filters.add(new AsApiFilterItem("user.loginId", activateLoginTest.getUser(), AsApiFilterItem.SortOrderEnum.ASCENDING,
				AsApiFilterItem.OperatorEnum.EQUALS));
		clientRestApi.queryCloudSafe(filters, 0, 100);

		setInfo("OK, :-)");
		return null;
	}

}
