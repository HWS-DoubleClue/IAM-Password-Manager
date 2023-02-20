package com.doubleclue.dcem.admin.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.subjects.TemplateSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;

@Named("templateView")
@SessionScoped
public class TemplateView extends DcemView {


	@Inject
	private TemplateSubject templateSubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private TemplateDialog templateDialog;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PostConstruct
	private void init() {

		templateDialog.setParentView(this);

		subject = templateSubject;

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, templateDialog, DcemConstants.TEMPLATE_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_COPY, resourceBundle, templateDialog, DcemConstants.TEMPLATE_DIALOG);

		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, templateDialog, DcemConstants.TEMPLATE_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_SHOW, resourceBundle, templateDialog, DcemConstants.TEMPLATE_DIALOG);

		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, templateDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);

//		addAutoViewAction(Constants.ACTION_SHOW, resourceBundle, templateDialog, AsConst.TEMPLATE_DIALOG);

	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}
		
}
