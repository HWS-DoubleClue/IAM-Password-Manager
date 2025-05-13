package com.doubleclue.dcem.dm.gui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.entities.CloudSafeLimitEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.DataUnit;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;
import com.doubleclue.dcem.dm.subjects.DmStorageSubject;

@SuppressWarnings("serial")
@Named("dmStorageView")
@SessionScoped
public class DmStorageView extends DcemView {

	@Inject
	private DmStorageSubject storageSubject;
	
	@Inject
	CloudSafeLogic cloudSafeLogic;
	
	@Inject
	OperatorSessionBean operatorSessionBean;
	
	@Inject
	DocumentManagementModule documentManagementModule;
	
	LocalDate expiryDate;
	
	private DataUnit selectedDataUnit = DataUnit.GIGABYTE;
	private int limitSize;


	@PostConstruct
	private void init() {
		subject = storageSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, null, "/modules/dm/workflowDialog.xhtml");
		// addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, dmWorkflowEntityDialog, "/modules/dm/workflowDialog.xhtml");
		// addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, dmWorkflowEntityDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		// this.setTopComposition("/mgt/modules/dm/workflowTitle.xhtml");
	}

	/*
	* This method is called when the view is displayed or reloaded
	*
	*/
	@Override
	public void reload() {

	}
	
	public void actionOk() {
		System.out.println("DmStorageView.actionOk()");
		String email = documentManagementModule.getPreferences().getMemoryManagementEmail();
		if (email == null || email.isBlank()) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "noEmailConfigured");
			return;
		}
		if (email.contains("@") == false) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "noEmailConfigured");
			return;
		}
		
		
		
		JsfUtils.addInfoMessage(DocumentManagementModule.RESOURCE_NAME, "Under Constraction");
	}
	
	public DataUnit[] getDataUnits() {
		return DataUnit.values();
	}

	public int getUsagePercentage() {
		CloudSafeLimitEntity cloudSafeLimitEntity = cloudSafeLogic.getCloudSafeLimitEntity(operatorSessionBean.getDcemUser().getId());
		long limit = cloudSafeLimitEntity != null ? cloudSafeLimitEntity.getLimit() : cloudSafeLogic.getDefaultUserLimit();
		long usage = cloudSafeLimitEntity != null ? cloudSafeLimitEntity.getUsed() : 0;
		return limit > 0 ? (int) ((usage * 100) / limit) : 0;
	}

	public LocalDateTime getStorageExpireDate() {
		CloudSafeLimitEntity cloudSafeLimitEntity = cloudSafeLogic.getCloudSafeLimitEntity(operatorSessionBean.getDcemUser().getId());
		return cloudSafeLimitEntity.getExpiryDate();
	}

	public String getFormattedLimit() {
		CloudSafeLimitEntity cloudSafeLimitEntity = cloudSafeLogic.getCloudSafeLimitEntity(operatorSessionBean.getDcemUser().getId());
		long limit = cloudSafeLimitEntity != null ? cloudSafeLimitEntity.getLimit() : cloudSafeLogic.getDefaultUserLimit();
		return DataUnit.getByteCountAsString(limit);
	}

	public String getFormattedUsage() {
		CloudSafeLimitEntity cloudSafeLimitEntity = cloudSafeLogic.getCloudSafeLimitEntity(operatorSessionBean.getDcemUser().getId());
		long usage = cloudSafeLimitEntity != null ? cloudSafeLimitEntity.getUsed() : 0;
		return DataUnit.getByteCountAsString(usage);
	}

	public LocalDate getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDate expiryDate) {
		this.expiryDate = expiryDate;
	}

	public DataUnit getSelectedDataUnit() {
		return selectedDataUnit;
	}

	public void setSelectedDataUnit(DataUnit selectedDataUnit) {
		this.selectedDataUnit = selectedDataUnit;
	}

	public int getLimitSize() {
		return limitSize;
	}

	public void setLimitSize(int limitSize) {
		this.limitSize = limitSize;
	}

}
