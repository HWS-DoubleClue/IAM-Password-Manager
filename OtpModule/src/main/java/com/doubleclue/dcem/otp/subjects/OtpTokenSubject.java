package com.doubleclue.dcem.otp.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.otp.entities.OtpTokenEntity;
import com.doubleclue.dcem.otp.logic.OtpModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class OtpTokenSubject extends SubjectAbs {

	public OtpTokenSubject() {

// rawActions.add(new RawAction (DcemConstants.ACTION_IMPORT, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.IGNORE));
		RawAction rawAction = new RawAction(DcemConstants.ACTION_IMPORT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN },
				ActionSelection.CREATE_OBJECT);
		rawAction.setIcon("fa fa-plus");
		rawActions.add(rawAction);
//		rawActions.add(new RawAction (DcemConstants.ACTION_ADD, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction (DcemConstants.ACTION_EDIT, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction (DcemConstants.ACTION_DELETE, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_OR_MORE));
				
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, null));
		rawActions.add (new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));
		
	}	

	@Override
	public String getModuleId() {
		return OtpModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 10;
	}

	
	@Override
	public String getIconName() {
		return "fa fa-credit-card";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}


	@Override
	public Class<?> getKlass() {
		return OtpTokenEntity.class;
	}

}
