package com.doubleclue.dcem.admin.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.primefaces.model.file.UploadedFile;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.TextMessage;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.TextResourceLogic;
import com.doubleclue.dcem.core.tasks.UpdateDbResources;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.hazelcast.core.IExecutorService;

@SuppressWarnings("serial")
@Named("textResourceDialog")
@SessionScoped
public class TextResourceDialog extends DcemDialog {

	@Inject
	private TextResourceLogic textResourceLogic;
	
	@Inject
	private ViewNavigator viewNavigator;

	// @Inject
	// private TextResourceView textResourceView;

	String displayLanguage = SupportedLanguage.English.name();

	private UploadedFile uploadedFile;

	@Override
	public boolean actionOk() throws Exception {
		TextMessage textMessage = (TextMessage) this.getActionObject();
		textResourceLogic.addOrUpdate(textMessage, this.getAutoViewAction().getDcemAction(), AdminModule.MODULE_ID, displayLanguage);

		try {
			// inform all other nodes
			IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
			executorService.executeOnAllMembers(new UpdateDbResources(TenantIdResolver.getCurrentTenantName()));

		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			return false;
		}

		return true;
	}
	
	public void actionDownload() throws Exception {
		Locale locale = DcemUtils.getLocaleFromDisplayName(displayLanguage);
		DbResourceBundle bundle = DbResourceBundle.getDbResourceBundle(locale);
		JsfUtils.downloadFile(MediaType.TEXT_HTML, "TextResource_" +  locale.getLanguage() +  ".properties", bundle.getPropertyContents());
//		viewNavigator.getActiveView().closeDialog();
		return;
	}

	/**
	 * @throws Exception
	 */
	public void actionUpload() throws Exception {
		if (uploadedFile == null || uploadedFile.getSize() == 0) {
			JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, AdminModule.RESOURCE_NAME, "Upload.noFile", null, null);
			return;
		}
		String fileName = uploadedFile.getFileName();
		if (fileName.endsWith(DcemConstants.TEXT_RESOURCE_FILE_TYPE) == false) {
			JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, AdminModule.RESOURCE_NAME, "Upload.rightEnd", null, null);
			return;
		}
		fileName = uploadedFile.getFileName().substring(0, fileName.length() - DcemConstants.TEXT_RESOURCE_FILE_TYPE.length());
		if (fileName.charAt(fileName.length()-3) != DcemConstants.TEXT_RESOURCE_FILE_SEPERATOR) {
			JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, AdminModule.RESOURCE_NAME, "Upload.fileEnd", null, null);
			return;
		}
		String isoLocale = fileName.substring(fileName.length()-2);
		

		Locale locale = DcemUtils.getLocaleFromDisplayName(displayLanguage);
		if (locale.getLanguage().equals(isoLocale) == false) {
			JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, AdminModule.RESOURCE_NAME, "Upload.noMatch", null, null);
			return;	
		}
		Properties properties = new Properties();
		ByteArrayInputStream bais = new ByteArrayInputStream(uploadedFile.getContent());
		InputStreamReader inputStreamReader = new InputStreamReader(bais, DcemConstants.CHARSET_UTF8);	
		properties.load(inputStreamReader);
		try {
			textResourceLogic.addTextResourceProperties(AdminModule.MODULE_ID, properties, locale, true);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			return;
		}

		try {
			// inform all other nodes
			IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
			executorService.executeOnAllMembers(new UpdateDbResources(TenantIdResolver.getCurrentTenantName()));

		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			return;
		}

		viewNavigator.getActiveView().closeDialog();
		return;
	}

	public boolean isEdit() {
		return this.getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT);
	}

	/* (non-Javadoc)
	 * @see com.doubleclue.dcem.core.gui.DcemDialog#show(com.doubleclue.dcem.core.gui.DcemView, com.doubleclue.dcem.core.gui.AutoViewAction)
	 */
	// @Override
	// public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
	// // if (this.getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT)) {
	// // if (((DcemTemplate) this.getActionObject()).isActive() == false) {
	// // throw new DcemException(DcemErrorCodes.CANNOT_CHANGE_TEMPLATE_IN_USE, "Cannot change Template which is not
	// // active.");
	// // }
	// // }
	// TextMessage textMessage = (TextMessage) this.getActionObject();
	// parentView = dcemView;
	// }

	public String getDisplayLanguage() {
		TextMessage textMessage = (TextMessage) this.getActionObject();
		if (textMessage != null && textMessage.getTextResourceBundle() != null) {
			return textMessage.getTextResourceBundle().getLocale();
		}
		return displayLanguage;
	}

	public void setDisplayLanguage(String displayLanguage) {
		this.displayLanguage = displayLanguage;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

}
