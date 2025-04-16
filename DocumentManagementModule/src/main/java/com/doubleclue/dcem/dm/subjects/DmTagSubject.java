package com.doubleclue.dcem.dm.subjects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.doubleclue.dcem.as.entities.CloudSafeTagEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.dm.gui.DmTagView;
import com.doubleclue.dcem.dm.logic.DmConstants;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class DmTagSubject extends SubjectAbs {

	@Inject
	DmTagView dmTagViewView;

	public DmTagSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.ONE_OR_MORE));

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		RawAction rawActionShowDocumentsWithTags = new RawAction(DmConstants.SHOW_DOCUMENT_WITH_TAG,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER },
				ActionSelection.ONE_ONLY);
		rawActionShowDocumentsWithTags.setActionType(ActionType.EL_METHOD);
		rawActionShowDocumentsWithTags.setElMethodExpression(DmConstants.DM_EL_METHOD_GOTO_DOCUMENTVIEW_WITH_TAGS);
		rawActionShowDocumentsWithTags.setIcon("fas fa-tag");
		rawActions.add(rawActionShowDocumentsWithTags);
	};

	@Override
	public String getModuleId() {
		return DocumentManagementModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 200;
	}

	@Override
	public String getIconName() {
		return "fas fa-tag";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return CloudSafeTagEntity.class;
	}
}
