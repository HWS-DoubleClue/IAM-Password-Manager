package com.doubleclue.dcem.as.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.DcemDialog;

@SuppressWarnings("serial")
@Named("msgShowDialog")
@SessionScoped
public class MsgShowDialog extends DcemDialog {
	

	@Override
	public boolean actionOk() throws Exception {

		return true;
	}

	public void leavingDialog() {
		
	} 

}
