package com.doubleclue.dcem.admin.gui;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.BranchLocation;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.BranchLocationLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;

@Named("branchLocationDialog")
@SessionScoped
public class BranchLocationDialog extends DcemDialog {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private Logger logger = LogManager.getLogger(BranchLocationDialog.class);

	@Inject
	private BranchLocationLogic branchLocationLogic;

	@Inject
	private DcemApplicationBean dcemApplicationBean;

	@Inject
	private AdminModule adminModule;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Override
	public boolean actionOk() throws Exception {
		BranchLocation branchLocation = (BranchLocation) this.getActionObject();
		branchLocationLogic.addOrUpdate(branchLocation, this.getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		BranchLocation branchLocation = (BranchLocation) this.getActionObject();
		if (action.equals(DcemConstants.ACTION_ADD)) {
			if (adminModule.getPreferences().getUserDefaultLanguage() == SupportedLanguage.German) {
				branchLocation.setCountry(DcemConstants.COUNTRY_CODE_GERMAN);
			} else {
				branchLocation.setCountry(DcemConstants.COUNTRY_CODE_MALTA);
			}
		}
	}
	
	@Override
	public void actionConfirm() throws Exception {
		List<Object> branchLocationsAsObjects = this.autoViewBean.getSelectedItems();
		List<BranchLocation> branchLocations = branchLocationsAsObjects.stream().map(obj -> (BranchLocation) obj).toList();
		branchLocationLogic.deleteBranchLocations(branchLocations, this.getAutoViewAction().getDcemAction());
	}

	public void leaving() {
	}

	public List<SelectItem> getAvailableCountries() {
		return dcemApplicationBean.getAvailableCountries(operatorSessionBean.getLocale());
	}
}
