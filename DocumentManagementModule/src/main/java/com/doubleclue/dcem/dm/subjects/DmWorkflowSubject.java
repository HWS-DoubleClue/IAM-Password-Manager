//
//
package com.doubleclue.dcem.dm.subjects;
//
import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.dm.logic.DmConstants;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;
import com.doubleclue.dcem.dm.entities.DmWorkflowEntity;

@SuppressWarnings("serial")
@ApplicationScoped
public class DmWorkflowSubject extends SubjectAbs {
	
	public DmWorkflowSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER },
				ActionSelection.IGNORE));	
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER },
				ActionSelection.ONE_OR_MORE));
		
		RawAction rawAction = new RawAction(DmConstants.ACTION_DOCUMENTS, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER },
				ActionSelection.IGNORE);
		rawAction.setIcon("fa fa-file-lines");
		rawAction.setActionType(ActionType.VIEW_LINK);
		rawActions.add(rawAction);
				
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	
		this.setHiddenMenu(true);
	}	

	@Override
	public String getModuleId() {
		return DocumentManagementModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 30;
	}

	
	@Override
	public String getIconName() {
		return "fa fa-cubes";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}


	@Override
	public Class<?> getKlass() {
		return DmWorkflowEntity.class;
	}

}
