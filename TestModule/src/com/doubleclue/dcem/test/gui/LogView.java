package com.doubleclue.dcem.test.gui;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.gui.UserDialogBean;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.test.subjects.LogSubject;

@SuppressWarnings("serial")
@Named("logView")
@SessionScoped
public class LogView extends DcemView {

	@Inject
	private LogSubject logSubject;

	@Inject
	private UserDialogBean userDialogBean;

	@PostConstruct
	private void init() {
		userDialogBean.setParentView(this);
	//	subject = logSubject;
		addAutoViewAction(DcemConstants.ACTION_CLEAR, null, null, null);
	}
}
