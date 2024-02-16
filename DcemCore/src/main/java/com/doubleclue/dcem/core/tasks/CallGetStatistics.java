package com.doubleclue.dcem.core.tasks;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

import com.doubleclue.dcem.core.logic.DiagnosticLogic;
import com.doubleclue.dcem.core.logic.ModuleStatistic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

@SuppressWarnings("serial")
public class CallGetStatistics implements Callable<List<ModuleStatistic>>, Serializable {

	@Override
	public List<ModuleStatistic> call() throws Exception {

		WeldRequestContext requestContext = null;

		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			requestContext = WeldContextUtils.activateRequestContext();
			DiagnosticLogic diagnosticLogic = CdiUtils.getReference(DiagnosticLogic.class);
			return diagnosticLogic.getNodeStatistics(true);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}

}
