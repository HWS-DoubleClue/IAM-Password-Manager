package com.doubleclue.dcem.core.tasks;

import java.io.Serializable;
import java.util.concurrent.Callable;

import com.doubleclue.dcem.core.logic.DiagnosticLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

@SuppressWarnings("serial")
public class CallGetLogFiles implements Callable<byte []>, Serializable {

	@Override
	public byte [] call() throws Exception {

		WeldRequestContext requestContext = null;

		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			requestContext = WeldContextUtils.activateRequestContext();
			DiagnosticLogic diagnosticLogic = CdiUtils.getReference(DiagnosticLogic.class);
			return diagnosticLogic.getLogFiles();
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}

}
