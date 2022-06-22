package com.doubleclue.dcem.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class ReverseProxySubject extends SubjectAbs {

	public ReverseProxySubject() {
		// rawActions.add (new RawAction (Constants.ACTION_ADD, new String []
		// {Constants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.CREATE_OBJECT));
		// rawActions.add (new RawAction (Constants.ACTION_EDIT, new String []
		// {Constants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.ONE_ONLY));
		RawAction rawAction = new RawAction(DcemConstants.ACTION_CLEAR_LOG,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.IGNORE);
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{reverseProxyDialog.clear()}");
		rawActions.add(rawAction);
		rawActions.add(new RawAction(DcemConstants.ACTION_CONFIGURE,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions
				.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
		rawAction.setIcon("fa fa-close");
		for (RawAction action : rawActions) {
			action.setMasterOnly(true);
		}
	}

	public String getModuleId() {
		return AsModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 150;
	}

	@Override
	public String getIconName() {
		return "fa fa-map-signs";
	}

	@Override
	public String getPath() {
		// return "/modules/asm/devices.xhtml";
		return AsConstants.REVERSE_PROXY_VIEW;
	}

	@Override
	public Class<?> getKlass() {
		return null;
	}

}
