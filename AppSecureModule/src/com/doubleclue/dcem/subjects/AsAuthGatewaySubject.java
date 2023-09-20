package com.doubleclue.dcem.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.as.entities.AuthGatewayEntity;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class AsAuthGatewaySubject extends SubjectAbs {

		
	public AsAuthGatewaySubject () {
		
		rawActions.add (new RawAction (DcemConstants.ACTION_ADD, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.CREATE_OBJECT));
		RawAction downloadAction = new RawAction(DcemConstants.ACTION_DOWNLOAD,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY);
		downloadAction.setActionType(ActionType.EL_METHOD);
		downloadAction.setIcon("fa fa-download");
		downloadAction.setElMethodExpression("#{authAppDialog.actionDownload()}");
		downloadAction.setAjax(false);
		rawActions.add(downloadAction);
		rawActions.add (new RawAction (DcemConstants.ACTION_EDIT, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.ONE_ONLY));
		rawActions.add (new RawAction (DcemConstants.ACTION_DELETE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.ONE_OR_MORE));
		rawActions.add (new RawAction (DcemConstants.ACTION_VIEW, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	
// rawActions.add (new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN}));	
		RawAction rawAction = new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN },
				ActionSelection.CREATE_OBJECT);
		rawActions.add(rawAction);
		
		rawActions.add (new RawAction (AsConstants.ACTION_ACTIVE_AUTH_GATEWAY, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN}));	

//		incudedDailogs.add(Constants.AUTO_DIALOG_PATH);
//		incudedDailogs.add(Constants.AUTO_CONFIRM_DIALOG_PATH);
	}



	public String getModuleId() {
		return AsModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 160;
	}	

	@Override
	public String getIconName() {
		return "fa fa-key";
	}

	@Override
	public String getPath() {
//		return "/modules/asm/devices.xhtml";
		return DcemConstants.AUTO_VIEW_PATH;
	}

	
	@Override
	public Class<?> getKlass() {
		return AuthGatewayEntity.class;
	}

}
