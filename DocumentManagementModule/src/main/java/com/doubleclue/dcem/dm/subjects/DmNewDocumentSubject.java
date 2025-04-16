//
//
package com.doubleclue.dcem.dm.subjects;

//
import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.dm.logic.DmConstants;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class DmNewDocumentSubject extends SubjectAbs {

	public DmNewDocumentSubject() {
		setHiddenMenu(true);
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER }));
		this.setHiddenMenu(true);	
	}

	@Override
	public String getModuleId() {
		return DocumentManagementModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 1;
	}

	@Override
	public String getIconName() {
		return "fa fa-file";
	}

	@Override
	public String getPath() {
		return DmConstants.DM_NEW_DOCUMENT_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return null;
	}

}
