package com.doubleclue.dcem.core.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.doubleclue.dcem.core.entities.EntityInterface;

@Named("genericDcemDialog")
@SessionScoped
public class GenericDcemDialog extends DcemDialog  {
	

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean actionOk() throws Exception {
		EntityInterface entity = (EntityInterface) getActionObject();
		jpaLogic.addOrUpdateEntity(entity, getAutoViewAction().getDcemAction());
		return true;
	}
	
	public void actionConfirm() throws Exception {
		jpaLogic.deleteEntities(autoViewBean.getSelectedItems(), getAutoViewAction().getDcemAction());		
	}
	
	
}
