package com.doubleclue.dcem.system.gui;

import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.logic.DiagnosticLogic;
@Named("diagnosticsResetDialog")
public class DiagnosticResetDialog extends DcemDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Inject
	private DiagnosticLogic diagnosticLogic;

//	@Override
//	public String getWidth() {
//		return "600";
//	}

	@Override
	public void actionConfirm() {
		diagnosticLogic.deleteDiagnostics();
		diagnosticLogic.resetCounters();
			
		return;
	}

}