package com.doubleclue.dcem.dev.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Entity;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.dev.logic.CreateTablesScripts;
import com.doubleclue.dcem.dev.logic.DevObjectTypes;
import com.doubleclue.dcem.dev.subjects.CreateCrudSubject;
import com.doubleclue.dcem.dev.subjects.CreateSqlTablesSubject;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.KaraUtils;

@SuppressWarnings("serial")
@Named("createSqlTablesView")
@SessionScoped
public class CreateSqlTablesView extends DcemView {

	@Inject
	CreateTablesScripts createTablesScripts;

	@Inject
	private CreateCrudView createCrudView;

	@Inject
	private CreateSqlTablesSubject createSqlTablesSubject;

	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		subject = createSqlTablesSubject;
	}

	public void actionOk() throws Exception {
		if (createCrudView.getSelectedDcemModule() == null) {
			JsfUtils.addErrorMessage("Please select a Module");
			return;
		}
		try {
			if (createCrudView.getSelectedDcemModule().getId() == AdminModule.MODULE_ID || createCrudView.getSelectedDcemModule().getId() == SystemModule.MODULE_ID) {
				JsfUtils.addErrorMessage("Cannot create tables for " +  createCrudView.getSelectedDcemModule().getName() + ". Please use the 'CreateTables' Utility");
				return;
			}
			createTablesScripts.createTables(createCrudView.getModuleDirectory(), createCrudView.getModuleResources(), createCrudView.getSelectedDcemModule());
			JsfUtils.addInfoMessage("Tables created successfull at " + createCrudView.getModuleResources() + " com.doubleclue.dcem.db");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
		return;
	}

}
