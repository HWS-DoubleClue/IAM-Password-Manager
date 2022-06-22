package com.doubleclue.dcem.test.units;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.as.restapi.model.AsApiActivationCode;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;

@ApplicationScoped
@Named("AddActivationCodeTest")
public class AddActivationCodeTest extends AbstractTestUnit {

	@Inject
	TestModule testModule;


	@Override
	public String getDescription() {

		return "Add activation code to Test";
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
		return TestUnitGroupEnum.LOGIN_AND_ACTIVATION;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	@Override
	public String start() throws Exception {
		super.start();		
		int count = 0;
		AsApiActivationCode activationCode;
		String code;
		for (; count < 10; count++) {
			activationCode = new AsApiActivationCode();
			activationCode.setUserLoginId("Test" + count);
			code = getClientRestApi().addActivationCode(activationCode);
			if (code == null) {
				throw new Exception ("return code is null");
			}
		}
		setInfo("Activation codes added: " + count);
		return null;
	}

}
