package com.doubleclue.dcem.admin.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.subjects.ReportingSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;

@SuppressWarnings("serial")
@Named("reportingView")
@SessionScoped
public class ReportingView extends DcemView {

	@Inject
	private ReportingSubject reportingSubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	DcemReportingLogic reportingLogic;
	
	@Inject
	OperatorSessionBean operatorSessionBean;
	
	@Inject
	AdminModule adminModule;

	@PostConstruct
	private void init() {
		ResourceBundle resourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		subject = reportingSubject;
		addAutoViewAction(DcemConstants.ACTION_EXCEL_EXPORT_ALL, resourceBundle, null,null);
		this.maxExport = adminModule.getPreferences().getMaxExport();
	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}
}
