//
//
package com.doubleclue.dcem.dm.subjects;
//
import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;


@SuppressWarnings("serial")
@ApplicationScoped
public class DmStorageSubject extends SubjectAbs {
	
	public DmStorageSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.IGNORE));	
						
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_USER}));
		rawActions.add(new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	
		this.setHiddenMenu(false);
	}	

	@Override
	public String getModuleId() {
		return DocumentManagementModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 40;
	}

	
	@Override
	public String getIconName() {
		return "fa fa-database";
	}

	@Override
	public String getPath() {
		return "/modules/dm/dmStorageView.xhtml";
	}


	@Override
	public Class<?> getKlass() {
		return null;
	}

}
