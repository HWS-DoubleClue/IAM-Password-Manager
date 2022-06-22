package com.doubleclue.dcem.test.units;

import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;

@ApplicationScoped
@Named("RequestAcTest")
public class RequestAcTest extends AbstractTestUnit {

	@Inject
	TestModule testModule;
	
	@Inject
	ActivateLoginTest activateLoginTest;

	
	@Override
	public String getDescription() {
		return "This unit will add 10 properties";
	}

	@Override
	public String getAuthor() {
		return "Emanuel Galea";
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

		setInfo("OK, :-)");
		return null;
	}

}
