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
import com.doubleclue.dcem.system.subjects.NodeSubject;

@Named("nodeView")
@SessionScoped
public class NodeView extends DcemView {


	@Inject
	private AutoViewBean autoViewBean;
	
	@Inject
	private NodeSubject nodeSubject;
	
	@Inject
	private NodeDialog nodeDialog;


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PostConstruct
	private void init() {

		nodeDialog.setParentView(this);

		subject = nodeSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		
		addAutoViewAction( DcemConstants.ACTION_ADD,  resourceBundle, nodeDialog, null);
		addAutoViewAction( DcemConstants.ACTION_EDIT,  resourceBundle, nodeDialog, null);
		addAutoViewAction( DcemConstants.ACTION_DELETE,  resourceBundle, nodeDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
//
//
	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}
	



}
