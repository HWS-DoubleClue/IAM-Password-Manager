package com.doubleclue.dcem.saml.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.saml.entities.SamlSpMetadataEntity;
import com.doubleclue.dcem.saml.logic.SamlConstants;
import com.doubleclue.dcem.saml.logic.SamlModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class SpMetadataSubject extends SubjectAbs {

	public SpMetadataSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_OR_MORE));

		RawAction downloadIdpMetadataAction = new RawAction(SamlConstants.ACTION_DOWNLOAD_IDP_METADATA, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.IGNORE);
		downloadIdpMetadataAction.setIcon(SamlConstants.ICON_DOWNLOAD_IDP_METADATA);
		rawActions.add(downloadIdpMetadataAction);
	}

	@Override
	public String getModuleId() {
		return SamlModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 0;
	}

	@Override
	public String getIconName() {
		return "fa fa-bookmark";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return SamlSpMetadataEntity.class;
	}
}
