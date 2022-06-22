package com.doubleclue.dcem.core.tasks;

import java.io.Serializable;

import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

@SuppressWarnings("serial")
public class ResetDiagCountersTask implements Runnable, Serializable {

	@Override
	public void run() {

		WeldRequestContext requestContext = null;

		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			requestContext = WeldContextUtils.activateRequestContext();
			
			DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
			for (DcemModule module : applicationBean.getSortedModules()) {
				module.resetDiagCounters();
			}
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}

}
