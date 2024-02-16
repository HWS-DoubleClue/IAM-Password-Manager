package com.doubleclue.dcem.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.as.entities.PolicyEntity;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class PolicySubject extends SubjectAbs {

	public PolicySubject () {
		rawActions.add(new RawAction (DcemConstants.ACTION_ADD, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction (DcemConstants.ACTION_EDIT, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_ONLY));

		rawActions.add(new RawAction (DcemConstants.ACTION_DELETE, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_OR_MORE));
		
		RawAction rawAction =  new RawAction (DcemConstants.ACTION_ASSIGN, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.IGNORE);
		rawAction.setIcon("fa fa-pencil");
		rawActions.add(rawAction);
		
		rawAction =  new RawAction (DcemConstants.ACTION_TEST_USER_POLICY, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.IGNORE);
		rawAction.setIcon("fa fa-question");
		rawActions.add(rawAction);
		
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));
		rawActions.add(new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	

	}

	public String getModuleId() {
		return AsModule.MODULE_ID;
	}

	public int getRank() {
		return 140;
	}

	public String getIconName() {
		return "fa fa-user-secret";
	}

	public String getPath() {
		return AsConstants.POLICY_VIEW_PATH;
	}

	public Class<?> getKlass() {
		return PolicyEntity.class;
	}

}
