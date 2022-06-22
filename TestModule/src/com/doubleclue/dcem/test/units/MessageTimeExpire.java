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
import com.doubleclue.as.restapi.model.AsMapEntry;
import com.doubleclue.dcem.as.logic.AsMessageLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;

@RequestScoped
@Named("MessageTimeExpire")

public class MessageTimeExpire extends AbstractTestUnit {

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
		return "This unit will add 3 Messages and let them expire;";
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

	public String start() throws Exception {
		AsClientRestApi clientRestApi = getClientRestApi();
		String user = activateLoginTest.getUser();
		

		asModule.getPreferences().setMessageResponseTimeout(5);
		asModule.getPreferences().setMessageRetrieveTimeoutSec(5);
		List<AddMessageResponse> resultList = new ArrayList<>();
		for (int i = 1; i < 4; i++) {
			List<AsMapEntry> dataMap2 = new ArrayList<>();
			dataMap2.add(new AsMapEntry("code", "FGHIJ"));
			AsApiMessage apiMessage = new AsApiMessage(user, "as.Login", dataMap2, true); 
			
			
			resultList.add(clientRestApi.addMessage(apiMessage));

		}

		setInfo("Ok");
		return null;

	}

}
