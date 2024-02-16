package com.doubleclue.dcem.admin.gui;

import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.licence.LicenceKeyContent;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logic.TenantLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiExecutionCallback;

@Named("adminLicenceDialog")
@SessionScoped
public class AdminLicenceDialog extends DcemDialog implements MultiExecutionCallback {

	@Inject
	LicenceLogic licenceLogic;

	@Inject
	AdminLicenceView licenceView;

	@Inject
	TenantLogic tenantLogic;

	private static final long serialVersionUID = 1L;
	private String licenceKey = "";
	private static Logger logger = LogManager.getLogger(AdminLicenceDialog.class);

	@Override
	public boolean actionOk() throws Exception {
		if (licenceKey == null) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "licence.error.keyEmpty");
			return false;
		}
		String trimmedLicenceKey = licenceKey.replaceAll("\\s+", ""); // remove all whitespace
		LicenceKeyContent licenceKeyContent = licenceLogic.getDecryptedLicence(trimmedLicenceKey);
		if (licenceKeyContent.getVersion() != DcemConstants.LICENCE_KEY_VERSION) {
			throw new DcemException(DcemErrorCodes.INVALID_LICENCE_KEY_VERSION,
					"Required Version: " + DcemConstants.LICENCE_KEY_VERSION);
		}
		TenantEntity tenantEntity;
		if (TenantIdResolver.isCurrentTenantMaster() == true) {
			int tenantId = Integer.parseInt(licenceView.getTenantOption());
			if (tenantId == 0) {
				tenantEntity = TenantIdResolver.getMasterTenant();
			} else {
				tenantEntity = tenantLogic.getTenantById(tenantId);
			}
		} else {
			tenantEntity = TenantIdResolver.getCurrentTenant();
		}
		if (tenantEntity == null || tenantEntity.getName().equalsIgnoreCase(licenceKeyContent.getTenantId()) == false) {
			throw new DcemException(DcemErrorCodes.INVALID_LICENCE_CONTENT, "Wrong Tenant");
		}
		licenceLogic.setLicence(licenceKeyContent, tenantEntity);
		Exception exception = DcemUtils.reloadTaskNodes(LicenceLogic.class, tenantEntity.getName());
		if (exception != null) {
			throw exception;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.doubleclue.dcem.core.gui.DcemDialog#show(com.doubleclue.dcem.core.gui.
	 * DcemView, com.doubleclue.dcem.core.gui.AutoViewAction)
	 */
	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		super.show(dcemView, autoViewAction);
		setLicenceKey("");
	}

	public String getLicenceKey() {
		return licenceKey;
	}

	public void setLicenceKey(String licenceKey) {
		this.licenceKey = licenceKey;
	}

	@Override
	public void onComplete(Map<Member, Object> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResponse(Member arg0, Object arg1) {
		// TODO Auto-generated method stub

	}
}
