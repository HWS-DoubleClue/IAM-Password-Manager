package com.doubleclue.dcem.admin.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.subjects.AdminLicenceSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.licence.LicenceKeyContent;
import com.doubleclue.dcem.core.licence.LicenceKeyContentUsage;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logic.TenantLogic;

@Named("adminLicenceView")
@SessionScoped
public class AdminLicenceView extends DcemView {

	
	private static final Logger logger = LogManager.getLogger(AdminLicenceView.class);
	

	@Inject
	private AdminLicenceDialog licenceDialog;
	
	@Inject
	private TenantLogic tenantLogic;
	
	private static final long serialVersionUID = 1L;

	private ResourceBundle resourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME);

	private String selectedQuarterOptions = "0";
	private String selectedTenantOptions = "0";
	
	LicenceKeyContent licenceKeyContent;
	
	LicenceKeyContentUsage licenceKeyContentUsage ;

	@Inject
	LicenceLogic licenceLogic;

	@Inject
	AdminLicenceSubject licenceSubject;

	@PostConstruct
	private void init() {
		subject = licenceSubject;
		licenceDialog.setParentView(this);
		addAutoViewAction(DcemConstants.ACTION_IMPORT_LICENCE_KEY, resourceBundle, licenceDialog,
				DcemConstants.LICENCE_DIALOG_PATH);
		try {
			licenceKeyContent = licenceLogic.getLicenceKeyContent();
		} catch (DcemException e) {
			logger.warn(e);
		}
	}
	
	@Override
	public void reload() {
		try {
			licenceKeyContent = licenceLogic.getLicenceKeyContent();
		} catch (DcemException e) {
			JsfUtils.addErrorMessage("No license found: " + e.toString());
		}
		TenantEntity tenantEntity;
		if (isCurrentMaster()) {
			tenantEntity = tenantLogic.getTenantById(Integer.parseInt(selectedTenantOptions));
		} else {
			tenantEntity = TenantIdResolver.getCurrentTenant();
		}
		
		try {
			licenceKeyContentUsage = licenceLogic.getTenantLicenceKeyUsage (tenantEntity);
			licenceKeyContent = licenceKeyContentUsage.getKeyContent();
		} catch (Exception e) {
			logger.warn("Couldn't get licenceKeyContentUsage", e);
			JsfUtils.addErrorMessage("Something went wrong. Exception: " + e.toString());
		}		
	}

	public List<SelectItem> getTenantOptions() {
		List<TenantEntity> list = tenantLogic.getAllTenants();
		List<SelectItem> selection = new ArrayList<>(list.size());
		for (TenantEntity entity : list) {
			selection.add(new SelectItem(entity.getId(), entity.getFullName()));
		}
		return selection;
	}
	
	public void setTenantOption(String selectedTenantOptions) {
		this.selectedTenantOptions = selectedTenantOptions;
		reload();
		PrimeFaces.current().ajax().update("licenceform:licenceGroup");
	}
	
	public String getQuarterOption() {
		return this.selectedQuarterOptions;
	}
	
	public String getTenantOption() {
		return this.selectedTenantOptions;
	}	
	
	public boolean isCurrentMaster() {
		return TenantIdResolver.isCurrentTenantMaster();
	}

	public LicenceKeyContent getLicenceKeyContent() {
		return licenceKeyContent;
	}

	public void setLicenceKeyContent(LicenceKeyContent licenceKeyContent) {
		this.licenceKeyContent = licenceKeyContent;
	}

	public LicenceKeyContentUsage getLicenceKeyContentUsage() {
		return licenceKeyContentUsage;
	}

	public void setLicenceKeyContentUsage(LicenceKeyContentUsage licenceKeyContentUsage) {
		this.licenceKeyContentUsage = licenceKeyContentUsage;
	}
}
