package com.doubleclue.dcem.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class AsDeviceSubject extends SubjectAbs {

	public AsDeviceSubject() {

		RawAction rawAction = new RawAction(DcemConstants.ACTION_ENABLE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY);
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{asDeviceDialog.enableDevice()}");
		rawAction.setIcon("fa fa-check");
		rawActions.add(rawAction);

		rawAction = new RawAction(DcemConstants.ACTION_DISABLE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY);
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{asDeviceDialog.disableDevice()}");
		rawAction.setIcon("fa fa-minus");
		rawActions.add(rawAction);
		
		rawAction = new RawAction(DcemConstants.ACTION_SHOW_PN_TOKEN, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY);
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{asDeviceDialog.showPushNotificationToken()}");
		rawAction.setIcon("fa fa-eye");
		rawActions.add(rawAction);

//		rawActions.add(new RawAction(AsConstants.ACTION_GOTO_CLOUDDATA,
//				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
//				ActionSelection.IGNORE, AsConstants.ACTION_GOTO_CLOUDDATA, ActionType.VIEW_LINK));

		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_OR_MORE));
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
		// incudedDailogs.add(Constants.AUTO_DIALOG_PATH);
		// incudedDailogs.add(Constants.AUTO_CONFIRM_DIALOG_PATH);
	}

	@Override
	public String getModuleId() {
		return AsModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 40;
	}

	@Override
	public String getIconName() {
		return "fa fa-tablet";
	}

	@Override
	public String getPath() {
		// return "/modules/asm/devices.xhtml";
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return DeviceEntity.class;
	}
}
