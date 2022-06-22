package com.doubleclue.dcem.test.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.test.logic.TestModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class DashboardSubject extends SubjectAbs {

	public DashboardSubject() {

		RawAction rawAction = new RawAction(DcemConstants.ACTION_RUN, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY);
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{dashboardView.runTestUnit()}");	
		
		rawAction = new RawAction(DcemConstants.ACTION_STOP, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY);
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{dashboardView.stopTestUnit()}");
		
		
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	
		
	}	

	@Override
	public String getModuleId() {
		return TestModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 20;
	}

	
	@Override
	public String getIconName() {
		return "user_headset.png";
	}

	@Override
	public String getPath() {
		return DcemConstants.TEST_DASHBOARD_VIEW_PATH;
	}


	@Override
	public Class<?> getKlass() {
		return null;
	}

}
