package com.doubleclue.dcem.test.units;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.model.AddMessageResponse;
import com.doubleclue.as.restapi.model.AsApiMessage;
import com.doubleclue.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.as.restapi.model.AsMapEntry;
import com.doubleclue.dcem.as.logic.AsMessageLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.doubleclue.sdk.api.ResponseMessage;
import com.doubleclue.sdk.api.SdkListenerMethods;

@RequestScoped
@Named("TenMessageTest")

public class TenMessageTest extends AbstractTestUnit {

	@Inject
	TestModule testModule;

	@Inject
	ActivateLoginTest activateLoginTest;

	@Inject
	AsModule asModule;

	@Inject
	AsMessageLogic messageLogic;

	@Override
	public String getDescription() {
		return "This unit will add 10 Messages to a User;";
	}

	@Override
	public String getAuthor() {
		return "Oliver Jahn";
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new LinkedList<String>();
		dependencies.add(AddUserWithActivationTest.class.getSimpleName());
		dependencies.add(ActivateLoginTest.class.getSimpleName());
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

		super.start();

		AsClientRestApi clientRestApi = getClientRestApi();
		String user = activateLoginTest.getUser();
		int queueLength = asModule.getPreferences().getMaxMessageQueueLength() - 1;
		clientRestApi.cancelUserMessages(user);
		// send messages from server to client
		List<AddMessageResponse> resultList = new ArrayList<>();
		for (int i = 0; i < queueLength; i++) {
			List<AsMapEntry> dataMap = new ArrayList<AsMapEntry>();
			dataMap.add(new AsMapEntry("code", "ABCDE" + i));

			// AsApiMessage Message to user - device
			AsApiMessage apiMessage = new AsApiMessage(user, "as.Login", dataMap, true);
			AddMessageResponse messageResponse = clientRestApi.addMessage(apiMessage);
			resultList.add(messageResponse);
			setInfo("TenMessageTest sendMessage id: " + messageResponse.getMsgId() + ", Ttl=" + messageResponse.getTimeToLive() + ", " + i);
		}

		// check that messages are received
		for (AddMessageResponse result : resultList) {

			setInfo("TenMessageTest - waiting for next MsgId=" + result.getMsgId());

			// receive the message
			try {
				appSdkListnerImplSync.waitFor(SdkListenerMethods.onReceiveMessage, 2000);
				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("TenMessageTest.start() ERROR: " + e.toString());
				throw e;
			}

			setInfo("TenMessageTest.start(): message received: ");

			ResponseMessage responseMessage = new ResponseMessage(result.getMsgId(), "OK", null);
			appSdkImplSync.sendMessageResponse(responseMessage);

			AsApiMessageResponse response = clientRestApi.getMessageResponse(result.getMsgId(), 0);
			if (response.getFinal() == false) {
				Thread.sleep(200);
				response = clientRestApi.getMessageResponse(result.getMsgId(), 0);
				if (response.getFinal() == false) {
					throw new Exception("No response returned in REST");
				}
			}
			setInfo("TenMessageTest - MsgReady MsgId=" + result.getMsgId());
		}
		setInfo("Ok");
		return null;
	}
}
