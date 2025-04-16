package com.doubleclue.dcem.dm.gui;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.dm.subjects.DmSharedDocumentSubject;

@SuppressWarnings("serial")
@Named("dmSharedDocumentView")
@SessionScoped
public class DmSharedDocumentView extends DcemView {

	@Inject
	DmSharedDocumentSubject dmSharedDocumentSubject;


	@Inject
	DmDocumentView dmDocumentView;


	@PostConstruct
	private void init() {
		subject = dmSharedDocumentSubject;
	}

	@Override
	public void reload() {
		dmDocumentView.setShareDocumentsMode(true);
	}

	
}
