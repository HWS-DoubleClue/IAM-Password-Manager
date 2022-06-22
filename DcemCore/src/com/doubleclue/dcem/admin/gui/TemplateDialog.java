package com.doubleclue.dcem.admin.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.logic.TemplateLogic;


@Named("templateDialog")
@SessionScoped
public class TemplateDialog extends DcemDialog {
	

	@Inject
	TemplateLogic asTemplateLogic;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean actionOk() throws Exception {
//		AsActivationCode activationCode = (AsActivationCode) this.getActionObject();		
		DcemTemplate template = (DcemTemplate) this.getActionObject();
		asTemplateLogic.addOrUpdateTemplate(template, this.getAutoViewAction().getDcemAction(), true);
		
//		JsfUtils.addInformationMessage(AsModule.RESOUCE_NAME, "activationDialog.success", activationCode.getActivationCode());
		return true;
	}
	
	
	public String getHeight () {
		return "650";
	}
	
	public String getWidth () {
		return "800";
	}
	
	public boolean isNewTemplateAction () {
		if (this.getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_SHOW)) {
			return false;
		}
		if (((DcemTemplate) this.getActionObject()).isInUse()) {
			return false;
		}
		return true;
	}
	
	public boolean isNewTemplateVersionAction () {
		if (this.getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_SHOW)) {
			return false;
		}
		if (((DcemTemplate) this.getActionObject()).isInUse()) {
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.doubleclue.dcem.core.gui.DcemDialog#show(com.doubleclue.dcem.core.gui.DcemView, com.doubleclue.dcem.core.gui.AutoViewAction)
	 */
	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		if (action.equals(DcemConstants.ACTION_EDIT)) {
			if (((DcemTemplate) this.getActionObject()).isActive() == false) {
				throw new DcemException(DcemErrorCodes.CANNOT_CHANGE_TEMPLATE_IN_USE, "Cannot change Template which is not active.");
			}
		}
		if (action.equals(DcemConstants.ACTION_COPY)) {
			((DcemTemplate) this.getActionObject()).setInUse(false);
		}
		parentView = dcemView;
	}
	
	
	
	



}
