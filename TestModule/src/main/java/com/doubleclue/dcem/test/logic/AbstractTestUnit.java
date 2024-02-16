package com.doubleclue.dcem.test.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.RestConnectionConfig;
import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.radius.client.RadiusClient;
import com.doubleclue.sdk.AppSdkImplSync;
import com.doubleclue.sdk.api.AppSdkSync;
import com.doubleclue.sdk.api.AsConstants;
import com.doubleclue.sdk.api.AsVersion;
import com.doubleclue.sdk.api.SdkListenerMethods;
import com.doubleclue.utils.KaraUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractTestUnit {

	@Inject
	TestModule testModule;
	
	private static final Logger logger = LogManager.getLogger(AbstractTestUnit.class);

	static protected AppSdkListnerImplSync appSdkListnerImplSync;
	static protected AppSdkSync appSdkImplSync = null;
	static protected Map<String, Object> appProperties = new HashMap<>();
	static protected byte[] sdkConfigDcem;
	static protected byte[] keyStoreBytes = null;
	static protected AsVersion asVersion;
	SdkListenerMethods methodReceived;

	TestStatus testStatus = TestStatus.Idle;

	String info;

	Date date;

	static private AsClientRestApi clientRestApi;

	public abstract String getDescription();

	public abstract String getAuthor();

	public abstract List<String> getDependencies();
	
	public abstract TestUnitGroupEnum getParent();
	
	public abstract boolean isRunnableTest();

	public AsClientRestApi getClientRestApi() throws JsonParseException, JsonMappingException, IOException {
		if (clientRestApi == null) {
			RestConnectionConfig restConnectionConfig = new RestConnectionConfig();
			ObjectMapper objectMapper = new ObjectMapper();
			restConnectionConfig = objectMapper.readValue(testModule.getPreferences().getRestConnectionConfig(), RestConnectionConfig.class);
			clientRestApi = AsClientRestApi.initilize(restConnectionConfig);
		}
		return clientRestApi;
	}

	public String start() throws Exception {
		// File testAppDirectory = new File(LocalPaths.getDcemHomeDir(), "testApp");
		return null;
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	public TestStatus getTestStatus() {
		return testStatus;
	}

	public void setTestStatus(TestStatus testStatus) {
		this.testStatus = testStatus;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public void addInfo(String addInfo) {
		info = info + "\n" + addInfo;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String toString() {
		return getName();
	}

	public SdkListenerMethods waitFor(SdkListenerMethods method, int time) throws Exception {

		return appSdkListnerImplSync.waitFor(method, time);

	}

	public void initializationSdk() throws DcemException, IOException {
		// if (appSdkImplSync == null) {
		appSdkImplSync = AppSdkImplSync.getInstance();
		asVersion = new AsVersion("TestUnit", 1, 0, 0);
		appSdkListnerImplSync = new AppSdkListnerImplSync();
		File testAppDirectory = new File(LocalPaths.getDcemHomeDir(), "testApp");
		if (testAppDirectory.exists() == false) {
			testAppDirectory.mkdirs();
		}
		InputStream inputStream;
		inputStream = new FileInputStream(new File(testAppDirectory, "SdkConfig.dcem"));
		sdkConfigDcem = KaraUtils.readInputStream(inputStream);
		appProperties.put(AsConstants.PROPERTY_HOME_DIRECTORY, testAppDirectory.getAbsolutePath());

		byte[] authGatewayConfigContent;
		try {
			inputStream = new FileInputStream(new File(testAppDirectory, AppSystemConstants.AuthConnectorFileName));
			authGatewayConfigContent = KaraUtils.readInputStream(inputStream);
			appProperties.put(AsConstants.PROPERTY_AUTH_CONFIG, authGatewayConfigContent);
		} catch (Exception exp) {
			logger.info("No Authconnector file found " + exp.toString());
		}
		
		// }
	}

	/**
	 * @param appStatus
	 * @param asErrorCode
	 * @param appErrorCode
	 * @throws Exception
	 */
	// protected void checkExceptions(AppStatus appStatus, AsErrorCodes asErrorCode, AppErrorCodes appErrorCode) throws
	// Exception {
	// if (appSdkListnerImplSync.getAppStatus() != appStatus) {
	// throw new Exception(methodReceived + ": Wrong AppStatus received: " + appSdkListnerImplSync.getAppStatus() + "
	// instead of: " + appStatus);
	// }
	// if (asErrorCode != null) {
	// AsException asException = appSdkListnerImplSync.getAsException();
	// if (asException.getErrorCode() != AsErrorCodes.SERVER_RESPONSE_ERROR) {
	// throw new Exception(methodReceived + ": Wrong AsErrorCode received: " + asException.getErrorCode() + " instead
	// of: " + asErrorCode);
	//
	// }
	// if (appErrorCode != null && ((AppException) asException.getCause()).getError().equals(appErrorCode.name()) ==
	// false) {
	// throw new Exception(methodReceived + ": Wrong AppErrorCode received: " + ((AppException)
	// asException.getCause()).getError() + " instead of: "
	// + appErrorCode);
	// }
	// }
	// }

	public static void setClientRestApi(AsClientRestApi clientRestApi) {
		AbstractTestUnit.clientRestApi = clientRestApi;
	}

	protected void logOutUser() throws Exception {
		appSdkImplSync.logoff(null);
		setInfo("Logged out user.");
	}
}
