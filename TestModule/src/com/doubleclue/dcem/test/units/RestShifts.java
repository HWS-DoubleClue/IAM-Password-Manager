package com.doubleclue.dcem.test.units;

import java.util.List;

import javax.faces.bean.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;

@Named("RestShifts")
public class RestShifts extends AbstractTestUnit {
	
	@Inject
	TestModule testModule;
	
	@Override
	public String getDescription() {
		
		return "This unit test the Resp-API Echo command";
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
		
			
		String echo = "1234567890abcdefghijklmnopqrstuvwxyzÖÄÜß";
		
		String echoRes = getClientRestApi().echo(echo);
		if (echoRes.equals(echo) == false) {
			throw new Exception ("Return Echo is wrong");
		}
		setInfo("OK");
		return null;
	}

}
