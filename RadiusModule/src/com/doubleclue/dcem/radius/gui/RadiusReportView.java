package com.doubleclue.dcem.radius.gui;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.GenericDcemDialog;
import com.doubleclue.dcem.radius.subjects.RadiusReportSubject;

@SuppressWarnings("serial")
@Named("radiusReportView")
@SessionScoped
public class RadiusReportView extends DcemView {

	@Inject
	private RadiusReportSubject radiusReportSubject;

	@Inject
	private GenericDcemDialog genericDcemDialog;

	@PostConstruct
	private void init() {

		genericDcemDialog.setParentView(this);
		subject = radiusReportSubject;

	}

}
