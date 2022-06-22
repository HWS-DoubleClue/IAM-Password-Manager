package com.doubleclue.dcem.test.units;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.spongycastle.cert.path.validations.ParentCertIssuedValidation;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.comm.thrift.AuthGatewayConfig;
import com.doubleclue.comm.thrift.ThriftAuthMethod;
import com.doubleclue.dcem.as.entities.AuthGatewayEntity;
import com.doubleclue.dcem.as.logic.AsAuthGatewayLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.doubleclue.sdk.api.Account;
import com.doubleclue.sdk.api.AppLoginResponse;
import com.doubleclue.sdk.api.AsException;
import com.doubleclue.sdk.api.ReceivedMessage;
import com.doubleclue.sdk.api.ResponseMessage;
import com.doubleclue.sdk.api.SdkListenerMethods;
import com.doubleclue.utils.SecureUtils;
import com.doubleclue.utils.ThriftUtils;

@ApplicationScoped
@Named("AuthConnectorTest")
public class AuthConnectorTest extends AbstractTestUnit {

	final static String DEVICE_NAME = "testDevice";
	final static String AUTHCONNECTOR_NAME = "TestModuleConnector";

	@Inject
	TestModule testModule;

	@Inject
	AddUserWithActivationTest userWithActivationTest;
	
	@Inject
	AsAuthGatewayLogic asAuthGatewayLogic;
	
	
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
		return null;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	@Override
	public String start() throws Exception {
		AuthGatewayEntity authGatewayEntity = asAuthGatewayLogic.getAuthAppEntitiy(AUTHCONNECTOR_NAME);
		if (authGatewayEntity == null) {
			authGatewayEntity = new AuthGatewayEntity();
			authGatewayEntity.setDisabled(false);
			authGatewayEntity.setName(AUTHCONNECTOR_NAME);
			DcemAction dcemAction = new DcemAction(DcemConstants.AS_MODULE_ID, null, DcemConstants.ACTION_ADD);
			asAuthGatewayLogic.addOrUpdateAuthApp(authGatewayEntity, dcemAction);
			
			AuthGatewayConfig authGatewayConfig = new AuthGatewayConfig(authGatewayEntity.getName(), ByteBuffer.wrap(SecureUtils.encryptDataCommon(authGatewayEntity.getSharedKey())), TenantIdResolver.getCurrentTenantName());
			byte [] authConnectorFile = ThriftUtils.serializeObject(authGatewayConfig, true);
			File testAppDirectory = new File(LocalPaths.getDcemHomeDir(), "testApp");
			Files.write(new File(testAppDirectory, AppSystemConstants.AuthConnectorFileName).toPath(), authConnectorFile);
		}
		
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
		
		appSdkListnerImplSync.resetMethod();
		appSdkImplSync.authenticateUser(ThriftAuthMethod.PUSH_APPROVAL.getValue(), account.getName(), userWithActivationTest.getInitialPassword(), null, "hws001LTEST");
		try {
			appSdkListnerImplSync.waitFor(SdkListenerMethods.onReceiveMessage, 5000);
			Thread.sleep(1000);
		} catch (Exception e) {
			System.out.println("TenMessageTest.start() ERROR: " + e.toString());
			throw e;
		}

		setInfo("Message received");
		
		ReceivedMessage receivedMessage = appSdkListnerImplSync.getReceivedMessage();
		ResponseMessage responseMessage = new ResponseMessage(receivedMessage.getId(), "ok", null);
		appSdkListnerImplSync.resetMethod();
		appSdkImplSync.sendMessageResponse(responseMessage);
		Thread.sleep(1000);
		
		try {
			appSdkListnerImplSync.waitFor(SdkListenerMethods.onAuthenticateUserMessageResponse, 8000);
			Thread.sleep(1000);
		} catch (Exception e) {
			System.out.println("TenMessageTest.start() ERROR: " + e.toString());
			throw e;
		}
		setInfo("OK :-)");
		
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
