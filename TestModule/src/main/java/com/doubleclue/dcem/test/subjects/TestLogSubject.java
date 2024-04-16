//
//
package com.doubleclue.dcem.test.subjects;
//
import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.entities.TestLog;

@SuppressWarnings("serial")
@ApplicationScoped
public class TestLogSubject extends SubjectAbs {
	
	public TestLogSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.IGNORE));	
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_OR_MORE));
				
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	
		
	}	

	@Override
	public String getModuleId() {
		return TestModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 30;
	}

	
	@Override
	public String getIconName() {
		return "fa fa-house";
	}

	@Override
	public String getPath() {
		return "/modules/test/testLogView.xhtml";
	}


	@Override
	public Class<?> getKlass() {
		return TestLog.class;
	}

}
