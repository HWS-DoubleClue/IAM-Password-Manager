package com.doubleclue.dcem.as.gui;

import java.util.Collection;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.AuthAppSession;
import com.doubleclue.dcem.core.gui.DcemDialog;

@SuppressWarnings("serial")
@Named("activeAuthGatewayDialog")
@SessionScoped
public class ActiveAuthGatewayDialog extends DcemDialog {
	

	@Inject
	private AsModule asModule;
	
	@Override
	public boolean actionOk() throws Exception {

		return true;
	}
	
	public Collection<AuthAppSession> getActiveAuthGateway() {
		return asModule.getTenantData().getAuthAppSessions().values();
//		return asMessageHandler.getMsgValues(null);
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
