package com.doubleclue.dcem.test.logic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.test.gui.DashboardView;

@ApplicationScoped
@Named("testExecutor")
public class TestExecutor implements Runnable {

	private final static Logger logger = LogManager.getLogger(DcemApplicationBean.class);

	public TestExecutor(List<AbstractTestUnit> testsList) {
		super();
		this.testsList = testsList;
	}

	Map<String, ScheduledFuture<?>> runningTestFuture = new HashMap<>();
	AbstractTestUnit runningTestUnit;
	List<String> dependencies;
	List<AbstractTestUnit> testsList;
	Future<Exception> future;
	DashboardView dashboardView;

	boolean cancelRequested = false;
	boolean errorOcurred = false;

	@Override
	public void run() {
		DashboardLogic dashboardLogic = CdiUtils.getReference(DashboardLogic.class);
		Map<String, AbstractTestUnit> testUnits = dashboardLogic.getTestUnitsMap();
		// TaskExecutor taskExecutor = CdiUtils.getReference(TaskExecutor.class);
		for (AbstractTestUnit testUnit : testsList) {
			errorOcurred = false;
			dependencies = testUnit.getDependencies();
			if (dependencies == null) {
				dependencies = new LinkedList<String>();
			}
			dependencies.add(testUnit.getName());
			for (String testName : dependencies) {
				if (errorOcurred == true) {
					break;
				}
				AbstractTestUnit testToRun = testUnits.get(testName);
				testToRun.setInfo("...");
				future = dashboardLogic.getTaskExecutor().submit(new UnitTask(testToRun));
				while (true) {
					try {
						Exception exp = future.get(4, TimeUnit.SECONDS);
						if (exp != null) {
							throw exp;
						}
					} catch (TimeoutException e) {
						continue;
					} catch (CancellationException exp) {
						cancelRequested = true;
						String msg = "Test units aborted. Cancelled test unit: " + testToRun.getName();
						testToRun.setInfo("CANCELLED");
						testToRun.setTestStatus(TestStatus.Cancelled);
						break;
					} catch (Exception e) {
						String msg = "Error on starting Test: " + testToRun.getName() + " Cause: " + e.toString();
						logger.fatal(msg, e);
						testToRun.setInfo("ERROR " + e.toString());
						testToRun.setTestStatus(TestStatus.Error);
						cancelRequested = true;
						break;
					}
				}
				if (cancelRequested) {
					break;
				}
			}
			if (cancelRequested) {
				break;
			}
		}
		AbstractTestUnit.appSdkImplSync.exit();
		dashboardLogic.closeRunningTestExecutor();

	}

	public void cancelTestUnits() {
		future.cancel(true);
		cancelRequested = true;
	}

	public String getRunningTestUnitName() {
		if (runningTestUnit == null) {
			return null;
		}
		return runningTestUnit.getName();
	}

	public AbstractTestUnit getRunningTestUnit() {
		return runningTestUnit;
	}

	public void setRunningTestUnit(AbstractTestUnit runningTestUnit) {
		this.runningTestUnit = runningTestUnit;
	}

	public boolean isErrorOcurred() {
		return errorOcurred;
	}

	public void setErrorOcurred(boolean errorOcurred) {
		this.errorOcurred = errorOcurred;
	}

}
