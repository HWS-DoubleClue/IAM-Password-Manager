package com.doubleclue.dcem.system.gui;

import java.util.Calendar;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.KeyStoreEntity;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.system.subjects.KeyStoreSubject;

@Named("keyStoreView")
@SessionScoped
public class KeyStoreView extends DcemView {


	@Inject
	private AutoViewBean autoViewBean;
	
	@Inject
	private KeyStoreSubject keyStoreSubject;
	
	@Inject
	private KeyStoreDialog keyStoreDialog;


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PostConstruct
	public void init() {

		keyStoreDialog.setParentView(this);

		subject = keyStoreSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		
		addAutoViewAction( DcemConstants.ACTION_GENERATE,  resourceBundle, keyStoreDialog, DcemConstants.GENERATE_KEYSTORE_DIALOG_PATH);
		addAutoViewAction( DcemConstants.ACTION_DOWNLOAD_PEM,  resourceBundle, keyStoreDialog, null);
		addAutoViewAction( DcemConstants.ACTION_DOWNLOAD_PK12,  resourceBundle, keyStoreDialog, null);
		addAutoViewAction( DcemConstants.ACTION_UPLOAD,  resourceBundle, keyStoreDialog, DcemConstants.UPLOAD_KEYSTORE_DIALOG_PATH);
		addAutoViewAction( DcemConstants.ACTION_SHOW_PASSWORD,  resourceBundle, keyStoreDialog, DcemConstants.SHOW_PASSWORD_DIALOG_PATH);
		addAutoViewAction( DcemConstants.ACTION_DELETE,  resourceBundle, keyStoreDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);

	}

	@Override
	public void reload() {
		autoViewBean.reload();
		
	}
	
	@Override
	public KeyStoreEntity createActionObject() {
		KeyStoreEntity keyStoreEntity = new KeyStoreEntity();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 50);
		keyStoreEntity.setExpiresOn(calendar.getTime());
		return keyStoreEntity;		
	}

	



}
