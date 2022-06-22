package com.doubleclue.dcem.test.logic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.weld.CdiUtils;

@ApplicationScoped
@Named("testDashboardLogic")
public class DashboardLogic {

	private final static Logger logger = LogManager.getLogger(DcemApplicationBean.class);
	
	@Inject
	TaskExecutor taskExecutor;

	Map<String, AbstractTestUnit> testUnits = null;
	List<AbstractTestUnit> testUnitsList;
	TestExecutor runningTestExecutor;
	Future<?> runnableFuture;
	
	public void runTestUnits(List<AbstractTestUnit> testUnitList) throws Exception {
		runningTestExecutor = new TestExecutor(testUnitList);
		runnableFuture = taskExecutor.submit(runningTestExecutor);
	}

	// TODO Needs to be adapted to the new structure, treetable
	public void reLoadTestUnits() throws FileNotFoundException, ClassNotFoundException, IOException {
		AbstractTestUnit.appSdkImplSync = null; // force reload off sdk config
		AbstractTestUnit.setClientRestApi(null); // force reload
		testUnits = new HashMap<>();
		List<Class<?>> classes = null;
		// classes = ClassFinder.find("org.apache.commons.io");
		classes = ClassFinder.find("com.doubleclue.dcem.test.units");
		for (Class<?> clazz : classes) {
			if (AbstractTestUnit.class.isAssignableFrom(clazz)) {
				AbstractTestUnit testUnit;
				try {
					testUnit = (AbstractTestUnit) clazz.newInstance();
				} catch (Exception e) {
					logger.warn(e);
					continue;
				}
				testUnit = CdiUtils.getReference(testUnit.getClass().getSimpleName());
				testUnit.setInfo(null);
				testUnit.setTestStatus(TestStatus.Idle);
				testUnits.put(testUnit.getName(), testUnit);
			}
		}
		testUnitsList = new ArrayList<AbstractTestUnit>(testUnits.values());
		return;
	}

	public void cancelTestUnits() {
		runningTestExecutor.cancelTestUnits();
	}

	public List<AbstractTestUnit> getTestUnits() throws FileNotFoundException, ClassNotFoundException, IOException {
		if (testUnits == null) {
			reLoadTestUnits();
		}
		return testUnitsList;
	}

	public Map<String, AbstractTestUnit> getTestUnitsMap() {
		return testUnits;
	}

	public String getRunningTestUnit() {
		if (runningTestExecutor == null) {
			return null;
		}
		return runningTestExecutor.getRunningTestUnitName();
	}

	public void closeRunningTestExecutor () {
		runningTestExecutor = null;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

}
