package com.doubleclue.dcem.core.gui;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.logic.JpaLogic;

public class DcemDialog implements Serializable {

	private static final Logger logger = LogManager.getLogger(DcemDialog.class);
	/**
	 * 
	 */
	@Inject
	protected AutoViewBean autoViewBean;

	@Inject
	protected ViewNavigator viewNavigator;

	@Inject
	protected JpaLogic jpaLogic;

	private static final long serialVersionUID = -1394282981492456785L;

	protected DcemView parentView;

	AutoViewAction autoViewAction;

	String height = null;
	String width = null;

	public DcemDialog() {
	}

	public void setParentView(DcemView sasView) {
		parentView = sasView;
	}

	public Object getActionObject() {
		return parentView.getActionObject();
	}

	public List<Object> getSelection() {
		return parentView.getSelection();
	}

	public Object getFirstSelectedObject() {
		List<Object> list = parentView.getSelection();
		if (list == null || list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
	}

	public Object getActionSubObject() {
		return parentView.getActionSubObject();
	}


	public String getDisplayName() {
		return autoViewAction.getActionText();
	}

	public String getTitle() {
		return autoViewAction.getActionText();
	}

	public AutoViewAction getAutoViewAction() {
		return autoViewAction;
	}

	public void setAutoViewAction(AutoViewAction autoViewAction) {
		this.autoViewAction = autoViewAction;
	}

	public String getConfirmText() {
		List<Object> selectedObjects = autoViewBean.getSelectedItems();
		if (selectedObjects == null || selectedObjects.isEmpty()) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (Object object : selectedObjects) {
			if (sb.length() > 1) {
				sb.append("<br>");
			}
			sb.append(object.toString());
		}
		return sb.toString();
	}

	public String getConfirmTextHeader() {
		String message = "AUTO_CONFIRM." + autoViewAction.getRawAction().getName();
		return JsfUtils.getMessageFromBundle(autoViewAction.getResourceBundle(), message);
	}

	public void dialogReturn(String messageString) {
		if (JsfUtils.getMaximumSeverity() <= 0) {
			viewNavigator.getActiveView().closeDialog();
			if (messageString != null) {
				JsfUtils.addFacesInformationMessage(messageString);
			}
		}
	}

	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		parentView = dcemView;
	}

	public boolean actionOk() throws Exception {
		EntityInterface entity = (EntityInterface) getActionObject();
		jpaLogic.addOrUpdateEntity(entity, getAutoViewAction().getDcemAction());
		return true;
	}

	public void actionConfirm() throws Exception {
		try {
			jpaLogic.deleteEntities(autoViewBean.getSelectedItems(), getAutoViewAction().getDcemAction());
		} catch (DcemException semExp) {
			if (semExp.getErrorCode() == DcemErrorCodes.CONSTRAIN_VIOLATION_DB) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "db.constrain.at.delete", (Object[]) null);
			} else {
				logger.info("Couldn't Delete", semExp);
				JsfUtils.addErrorMessage(semExp.toString());
			}
		}
	}

	public void setSubActionObject(Object subObject) {
		// can be overwritten

	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public void leavingDialog() {
	}

	public void setActionObject(Object selectedObject) {
	}
}
