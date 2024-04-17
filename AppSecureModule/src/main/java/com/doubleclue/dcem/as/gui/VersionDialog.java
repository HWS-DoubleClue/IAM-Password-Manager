package com.doubleclue.dcem.as.gui;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.comm.thrift.ClientType;
import com.doubleclue.dcem.as.entities.AsVersionEntity;
import com.doubleclue.dcem.as.logic.AsVersionLogic;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.utils.ProductVersion;

@SuppressWarnings("serial")
@Named("versionDialog")
@SessionScoped
public class VersionDialog extends DcemDialog {


	@Inject
	private AsVersionLogic versionLogic;
	
	AsVersionEntity orgVersionEntity;
	
	// private static final Logger logger =
	// LogManager.getLogger(VersionDialog.class);

	public boolean actionOk() throws Exception {
		AsVersionEntity versionEntity = (AsVersionEntity) this.getActionObject();
		if (versionEntity.isTestApp()) {
			PrimeFaces.current().executeScript("PF('confirmTestApp').show();");
			return false;
		}

		versionEntity.setUser(null);
		ProductVersion productVersion = new ProductVersion(versionEntity.getName(), versionEntity.getVersionStr());
		versionEntity.setVersion(productVersion.getVersionInt());
		versionEntity.setVersionStr(productVersion.getVersionStr()); // remove the leading zeros
		versionLogic.addUpdateVersion(versionEntity, getAutoViewAction().getDcemAction());
		versionLogic.replicateTenantVersion(orgVersionEntity, versionEntity);
		return true;
	}
	
	public void actionConfirmTestApp () {
		try {
		AsVersionEntity versionEntity = (AsVersionEntity) this.getActionObject();
		versionEntity.setUser(null);
		ProductVersion productVersion = new ProductVersion(versionEntity.getName(), versionEntity.getVersionStr());
		versionEntity.setVersion(productVersion.getVersionInt());
		versionEntity.setVersionStr(productVersion.getVersionStr()); // remove the leading zeros
		versionLogic.addUpdateVersion(versionEntity, getAutoViewAction().getDcemAction());
		versionLogic.replicateTenantVersion(orgVersionEntity, versionEntity);
		PrimeFaces.current().executeScript("PF('confirmTestApp').show();");
		viewNavigator.getActiveView().closeDialog();
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}
		
	}

	public ClientType[] getClientTypes() {
		return ClientType.values();
	}
	
	public void actionConfirm() throws Exception {
		super.actionConfirm();
		List<Object> deletedVersions = autoViewBean.getSelectedItems();
		for (Object object : deletedVersions) {
			versionLogic.replicateTenantVersion((AsVersionEntity)object, null);
		}		
	}

		
	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		// TODO Auto-generated method stub
		super.show(dcemView, autoViewAction);
		orgVersionEntity = (AsVersionEntity) ((AsVersionEntity) this.getActionObject()).clone();
	}
	
	@Override
	public String getHeight() {
		return "500px";
	}

}
