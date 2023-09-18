package com.doubleclue.dcem.system.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.KeyStoreEntity;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.system.logic.SystemModule;

@ApplicationScoped
public class KeyStoreSubject extends SubjectAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public KeyStoreSubject() {

		RawAction rawAction = new RawAction(DcemConstants.ACTION_GENERATE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.CREATE_OBJECT);
		rawAction.setIcon("fa fa-plus-square-o");
		rawActions.add(rawAction);

		rawAction = new RawAction(DcemConstants.ACTION_UPLOAD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.CREATE_OBJECT);
		rawAction.setIcon(DcemConstants.ACTION_UPLOAD_ICON);
		rawActions.add(rawAction);
		
		rawAction = new RawAction(DcemConstants.ACTION_SHOW_PASSWORD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-eye");
		rawActions.add(rawAction);
				
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY));
		
		rawAction = new RawAction(DcemConstants.ACTION_DOWNLOAD_PK12, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY);
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{keyStoreDialog.downloadPk12()}");
		rawAction.setIcon("fa fa-download");
		rawAction.setAjax(false);
		rawActions.add(rawAction);
		
		rawAction = new RawAction(DcemConstants.ACTION_DOWNLOAD_PEM, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY);
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{keyStoreDialog.downloadPem()}");
		rawAction.setIcon("fa fa-download");
		rawAction.setAjax(false);
		rawActions.add(rawAction);
		
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
				
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
		for (RawAction action: rawActions) {
			action.setMasterOnly(true);
		}
	
	}

	public String getModuleId() {
		return SystemModule.MODULE_ID;
	}
	

	public int getRank() {
		return 30;
	}

	@Override
	public String getIconName() {
		return "fa fa-certificate";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	
	@Override
	public Class<?> getKlass() {
		return KeyStoreEntity.class;
	}
		
}
