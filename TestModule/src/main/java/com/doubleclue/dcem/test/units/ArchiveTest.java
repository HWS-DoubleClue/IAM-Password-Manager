package com.doubleclue.dcem.test.units;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.comm.thrift.AppErrorCodes;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.jpa.ExportRecords;
import com.doubleclue.dcem.core.jpa.JpaEntityCacheLogic;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;

@ApplicationScoped
@Named("ArchiveTest")
public class ArchiveTest extends AbstractTestUnit {

	@Inject
	TestModule testModule;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	ExportRecords exportRecords;

	@Inject
	JpaEntityCacheLogic cacheLogic;

	@Override
	public String getDescription() {
		return "This unit test to Test the Archives";
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
		return null;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	@Override
	public String start() throws Exception {
		DcemReporting reporting = new DcemReporting(ReportAction.Activation, (DcemUser) null, AppErrorCodes.INCORRECT_STATE, null, null);
		reporting.setLocalDateTime(LocalDateTime.now().minusDays(4));
		int archiveCount = DcemConstants.MAX_ARCHIVE_RECORDS + 20;
		for (int i = 0; i < archiveCount; i++) {
			reportingLogic.addReporting(reporting);
		}
//		reportingLogic.flushCache();
		String[] result = exportRecords.archive(2, DcemReporting.class, DcemReporting.GET_AFTER, DcemReporting.DELETE_AFTER);
		if (result == null) {
			throw new Exception("Nothing was archived");
		}
		int records = Integer.parseInt(result[1]);
		if (records < archiveCount) {
			throw new Exception("Archived " + records + " out of ");
		}
		setInfo("Test passed. Archived " + records + " records.");
		return null;
	}
}
