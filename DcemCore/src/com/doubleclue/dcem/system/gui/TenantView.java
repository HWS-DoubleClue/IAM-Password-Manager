//#excludeif COMMUNITY_EDITION
package com.doubleclue.dcem.system.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.system.subjects.TenantSubject;

@SuppressWarnings("serial")
@Named("tenantView")
@SessionScoped
public class TenantView extends DcemView {

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private TenantSubject tenantSubject;

	@Inject
	private TenantDialog tenantDialog;

	@PostConstruct
	private void init() {
		tenantDialog.setParentView(this);
		subject = tenantSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, tenantDialog, DcemConstants.SYSTEM_TENANT_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, tenantDialog, DcemConstants.SYSTEM_TENANT_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, tenantDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_RECOVER_SUPERADMIN_ACCESS, resourceBundle, tenantDialog, DcemConstants.TENANT_RECOVER_SUPERADMIN_ACCESS_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_SWITCH_TO_TENANT, resourceBundle, tenantDialog, null);

	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}
}
