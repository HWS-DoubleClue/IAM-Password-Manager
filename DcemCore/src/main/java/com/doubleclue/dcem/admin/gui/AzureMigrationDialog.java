package com.doubleclue.dcem.admin.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoDialogBean;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.DomainApi;
import com.doubleclue.dcem.core.logic.DomainAzure;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.DomainType;
import com.doubleclue.dcem.core.logic.DomainUsers;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.logic.SystemModule;

@SuppressWarnings("serial")
@Named("azureMigrationDialog")
@SessionScoped
public class AzureMigrationDialog extends DcemDialog {

	@Inject
	DomainLogic domainLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	AutoDialogBean autoDialogBean;

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	SystemModule systemModule;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	UserLogic userLogic;

	private static final Logger logger = LogManager.getLogger(AzureMigrationDialog.class);

	String azureDomain;
	
	List<MigrationUserStatus> listMigrationUserStatus;

	public AzureMigrationDialog() {
	}

	public void actionStartMigration() throws Exception {
		DomainEntity adDomainEntity = (DomainEntity) getActionObject();
		DomainEntity azureDomainEntity = getSelectedAzureDomain();
		try {
			listMigrationUserStatus = domainLogic.migrateAdToAzure(adDomainEntity, azureDomainEntity);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			logger.error("", e);
		}
	}
	
	public List<MigrationUserStatus> getUserStatus() {
		return listMigrationUserStatus;
	}

	@Override
	public String getWidth() {
		return "840";
	}

	@Override
	public String getHeight() {
		return "760";
	}

	public List<SelectItem> getAzureDomains() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		for (DomainEntity domainEntity : domainLogic.getDomainEntities()) {
			if (domainEntity.getDomainType() == DomainType.Azure_AD) {
				list.add(new SelectItem(domainEntity.getName(), domainEntity.getName()));
			}
		}
		return list;
	}

	private DomainEntity getSelectedAzureDomain() {
		for (DomainEntity domainEntity : domainLogic.getDomainEntities()) {
			if (domainEntity.getName().equals(azureDomain)) {
				return domainEntity;
			}
		}
		return null;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		DomainEntity domainEntity = (DomainEntity) getActionObject();
		if (domainEntity.getDomainType() != DomainType.Active_Directory) {
			throw new Exception("Only Active Directory Domains can be exportd to Azure");
		}

		if (getAzureDomains().isEmpty() == true) {
			throw new Exception("Please add an Azure Domain first!");
		}
		listMigrationUserStatus = null;
	}

	public String getAzureDomain() {
		return azureDomain;
	}

	public void setAzureDomain(String azureDomain) {
		this.azureDomain = azureDomain;
	}
	
	@Override
	public void leavingDialog() {
		listMigrationUserStatus = null;
	}

}
