//#excludeif COMMUNITY_EDITION == true
package com.doubleclue.dcem.admin.gui;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.subjects.AuditingSubject;
import com.doubleclue.dcem.core.gui.DcemView;

@Named("auditingView")
@SessionScoped
public class AuditingViewBean extends DcemView {
	
	@Inject
	AuditingSubject auditingSubject;


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PostConstruct
	private void init() {
		subject = auditingSubject;
	}

	

}
