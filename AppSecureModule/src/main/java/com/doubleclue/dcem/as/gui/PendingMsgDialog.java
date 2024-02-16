package com.doubleclue.dcem.as.gui;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.logic.MsgGui;
import com.doubleclue.dcem.core.gui.DcemDialog;

@SuppressWarnings("serial")
@Named("pendingMsgDialog")
@SessionScoped
public class PendingMsgDialog extends DcemDialog {
	
	@Inject
	private AsMessageHandler asMessageHandler;
	
	@Override
	public boolean actionOk() throws Exception {

		return true;
	}
	
	public List<MsgGui> getMsgs() {
		return asMessageHandler.getMsgValues(null);
	}
	
	public String getHeight() {
		return "600";
	}
	public String getWidth() {
		return "1000";
	}
	
	public void leavingDialog() {
		
	} 

}
