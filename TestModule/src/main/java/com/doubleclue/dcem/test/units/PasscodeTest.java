package com.doubleclue.dcem.test.units;

import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.dcem.as.logic.AsMessageLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;

@RequestScoped
@Named("PasscodeTest")

public class PasscodeTest extends AbstractTestUnit {

	@Inject
	TestModule testModule;

	@Inject
	ActivateLoginTest activateLoginTest;

	@Inject
	AsModule asModule;

	@Inject
	AsMessageLogic messageLogic;
	
	@Inject
	AddUserWithActivationTest userWithActivationTest;

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
	
//		PasscodeResponse passcodeResponse = appSdkImpl.getPasscode(user);
//		clientRestApi.verifyUser(user, userWithActivationTest.getInitialPassword(), passcodeResponse.getPasscode());
//
//		// try a wrong one
//		try {
//			clientRestApi.verifyUser(user, userWithActivationTest.getInitialPassword(), passcodeResponse.getPasscode()+1);
//			throw new Exception ("Wow, Acceptance of an invalid passcode");
//		} catch (AsApiException  exp) {
//			System.out.println("PasscodeTest.start()" + exp.toString());
//		}  catch (ApiException exp) {
//			System.out.println("PasscodeTest.start()" + exp.toString());
//		}
		

		
		setInfo("Ok");
		return null;

	}

}
