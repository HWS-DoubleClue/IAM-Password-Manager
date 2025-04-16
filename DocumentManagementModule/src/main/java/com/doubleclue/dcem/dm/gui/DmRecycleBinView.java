package com.doubleclue.dcem.dm.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.CloudSafeDto;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.dm.logic.DmSolrLogic;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;
import com.doubleclue.dcem.dm.subjects.DmRecycleBinSubject;

@SuppressWarnings("serial")
@Named("dmRecycleBinView")
@SessionScoped
public class DmRecycleBinView extends DcemView {

	@Inject
	DmRecycleBinSubject dmRecycleBinSubject;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	DmSolrLogic dmSolrLogic;

	private ResourceBundle resourceBundle;
	private List<CloudSafeEntity> trashedFiles;
	private List<CloudSafeEntity> selectedFiles;
	CloudSafeEntity toOpenFileorFolder;

	String searchTerm;


	@PostConstruct
	private void init() {
		subject = dmRecycleBinSubject;
		resourceBundle = JsfUtils.getBundle(DocumentManagementModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	@Override
	public void reload() {
		trashedFiles = null;
	}
	
	public void actionSearch() {
		trashedFiles = null;
	}

	public List<CloudSafeEntity> getTrashedFiles() {
		if (trashedFiles != null) {
			return trashedFiles;
		}
		try {
			trashedFiles = cloudSafeLogic.getCloudSafeByUserFlat(operatorSessionBean.getDcemUser(), operatorSessionBean.getUserGroups(), true);
			for (CloudSafeEntity cloudSafeEntity : trashedFiles) {
				cloudSafeEntity.getLocation();
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.toString());
			trashedFiles = new ArrayList<CloudSafeEntity>();
		}
		return trashedFiles;
	}

	public void actionPrepareConfirmation() {
		if (selectedFiles == null || selectedFiles.isEmpty() == true) {
			JsfUtils.addErrorMessage(resourceBundle.getString("documentView.error.noFileSelected"));
			return;
		} 
		showDialog("confirmDlg");
		PrimeFaces.current().ajax().update("deleteDialog:deleteTextMessage");
	}

	public void actionOpenRecoverDialog() {
		if (selectedFiles == null || selectedFiles.isEmpty() == true) {
			JsfUtils.addErrorMessage(resourceBundle.getString("documentView.error.noFileSelected"));
		} else {
			showDialog("recoverDlg");
			PrimeFaces.current().ajax().update("recoverDialog:deleteTextMessage");
		}
	}

	public void deleteCloudSafeFiles() {
		try {
			List<CloudSafeDto>  deletedDbFiles = cloudSafeLogic.deleteFiles(selectedFiles, operatorSessionBean.getDcemUser());
			cloudSafeLogic.deleteCloudSafeFilesContent(deletedDbFiles);
		} catch (Exception e) {
			logger.warn("Couldn't delete files", e);
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.deleteFailed" + e.getLocalizedMessage());
			return;
		}
		trashedFiles = null;
		selectedFiles.clear();
		hideDialog("confirmDlg");
	}

	public void actionRecoverSelectedFiles() {
	    try {
	        List<CloudSafeDto> recoveredFiles = cloudSafeLogic.recoverCloudSafeFiles(selectedFiles);
	        try {
	            dmSolrLogic.indexUserDocuments(recoveredFiles, operatorSessionBean.getDcemUser(), operatorSessionBean.getUserGroups());
	        } catch (Exception indexingException) {
	            JsfUtils.addWarnMessage("Some files were recovered but could not be indexed. Please contact the administrator.");
	        }
	        trashedFiles = null;
	    } catch (DcemException recoveryException) {
	        JsfUtils.addErrorMessage(recoveryException.getLocalizedMessage());
	    } finally {
	        hideDialog("recoverDlg");
	    }
	}
	
	

	public void setDeletedFiles(List<CloudSafeEntity> deleteFiles) {
		this.trashedFiles = deleteFiles;
	}

	public List<CloudSafeEntity> getSelectedFiles() {
		return selectedFiles;
	}

	public void setSelectedFiles(List<CloudSafeEntity> selectedFiles) {
		this.selectedFiles = selectedFiles;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

}
