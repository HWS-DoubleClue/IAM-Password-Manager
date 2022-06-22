package com.doubleclue.dcem.system.gui;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.DiagnosticLogic;
import com.doubleclue.dcem.core.logic.ModuleStatistic;
import com.doubleclue.dcem.system.logic.StatisticCounterHelper;
import com.doubleclue.dcem.system.logic.StatisticValueHelper;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.dcem.system.logic.SystemPreferences;
import com.doubleclue.dcem.system.subjects.DiagnosticsSubject;

@Named("diagnosticsView")
@SessionScoped
public class DiagnosticsView extends DcemView {

	private static final Logger logger = LogManager.getLogger(DiagnosticsView.class);

	private static final String CHARTS_DIALOG = "/modules/system/diagnosticsChartsDialog.xhtml";

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private DiagnosticLogic diagnosticLogic;

	@Inject
	private KeyStoreDialog keyStoreDialog;

	@Inject
	private DiagnosticsSubject diagnosticsSubject;

	// #if COMMUNITY_EDITION == false
	@Inject
	private DiagnosticChartsDialog diagnosticsChartsDialog;
	// #endif

	@Inject
	private DiagnosticResetDialog diagnosticsResetDialog;

	@Inject
	private SystemModule systemmodule;

	private Date from;
	private Date to;

	private Map<String, List<ModuleStatistic>> statistics;

	private List<StatisticCounterHelper> counters;

	private List<StatisticValueHelper> values;

	private List<StatisticValueHelper> staticValues;

	private static final long serialVersionUID = 1L;

	private String diagnosticTime = DiagnosticLogic.CURRENT_TIME;

	List<SelectItem> diagnosticTimes;

	@PostConstruct
	private void init() {
		subject = diagnosticsSubject;
		diagnosticsResetDialog.setParentView(this);
		keyStoreDialog.setParentView(this);
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);

		// addAutoViewAction(DcemConstants.ACTION_SAVE, resourceBundle, null, null);
		addAutoViewAction(DcemConstants.ACTION_RESET_COUNTERS, resourceBundle, diagnosticsResetDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);	
		addAutoViewAction(DcemConstants.ACTION_DOWNLOAD_LOG_FILE, resourceBundle, null, null);
		// #if COMMUNITY_EDITION == false
		addAutoViewAction(DcemConstants.ACTION_DOWNLOAD_DIAGNOSTIC_FILE, resourceBundle, null, null);
		addAutoViewAction(DcemConstants.ACTION_SHOW_DIAGNOSTIC_CHARTS, resourceBundle, diagnosticsChartsDialog, CHARTS_DIALOG);
		// #endif
	}

	@Override
	public void reload() {
		if (diagnosticTime.equals(DiagnosticLogic.CURRENT_TIME)) {
			statistics = diagnosticLogic.collect(false, false);
			convertStaticValues();
		} else {
			try {
				statistics = diagnosticLogic.getNodeStatisticDb(diagnosticTime);
			} catch (Exception e) {
				statistics = new HashMap<>();
				JsfUtils.addErrorMessage(e.toString());
			}
		}
		convertCounters();
		convertValues();
		diagnosticTimes = null;
		autoViewBean.reload();
	}

	private void convertCounters() {
		Date date = new Date();
		List<ModuleStatistic> list;
		counters = new LinkedList<>();
		for (String node : statistics.keySet()) {
			list = statistics.get(node);
			for (ModuleStatistic module : list) {
				for (String name : module.getCounters().keySet()) {
					counters.add(new StatisticCounterHelper(date, node, module.getModuleId(), name, module.getCounters().get(name)));
				}
			}
		}
	}

	// #if COMMUNITY_EDITION == false
	@Override
	public void triggerAction(AutoViewAction autoViewAction) {
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_SHOW_DIAGNOSTIC_CHARTS)) {
			SystemPreferences modulePreferences = systemmodule.getPreferences();
			if (modulePreferences.isEnableMonitoring() == false) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "IS_ENABLE_Monitoring");
				autoViewAction = null;
			} else {
				boolean foundSelected = false;
				for (StatisticValueHelper statisticValueHelper : getValues()) {
					if (statisticValueHelper.isChecked()) {
						foundSelected = true;
						break;
					}
				}
				if (foundSelected == false) {
					JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "MULTIPLE_CHECKBOXES_SELECTION");
					autoViewAction = null;
				}
			}
		}
		super.triggerAction(autoViewAction);
	}
	
	public void downloadDiagnosticFile() {
		String data;
		String thisTime;
		try {
			data = diagnosticLogic.getDiagnosticFile(diagnosticTime);
			if (diagnosticTime.equals(DiagnosticLogic.CURRENT_TIME)) {
				thisTime = "Current-" + diagnosticLogic.getDateFormatFile().format(new Date());
			} else {
				Date date = diagnosticLogic.getDateFormat().parse(diagnosticTime);
				thisTime = diagnosticLogic.getDateFormatFile().format(date);
			}
			JsfUtils.downloadFile(MediaType.APPLICATION_JSON, "DCEM_Diagnostic_" + thisTime + ".json", data.getBytes("UTF-8"));
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
	}
	
	public List<SelectItem> getDiagnosticTimes() {
		if (diagnosticTimes == null) {
			diagnosticTimes = diagnosticLogic.getDiagnosticTimes();
		}
		return diagnosticTimes;

	}
	// #endif
	
	private void convertValues() {
		Date date = new Date();
		List<ModuleStatistic> list;
		values = new LinkedList<>();
		for (String node : statistics.keySet()) {
			list = statistics.get(node);
			for (ModuleStatistic module : list) {
				for (String name : module.getValues().keySet()) {
					values.add(new StatisticValueHelper(date, node, module.getModuleId(), name, module.getValues().get(name)));
				}
			}
		}
	}

	private void convertStaticValues() {
		Date date = new Date();
		List<ModuleStatistic> list;
		staticValues = new ArrayList<>();
		for (String node : statistics.keySet()) {
			list = statistics.get(node);
			for (ModuleStatistic module : list) {
				for (String name : module.getStaticValues().keySet()) {
					staticValues.add(new StatisticValueHelper(date, node, module.getModuleId(), name, module.getStaticValues().get(name)));
				}
			}
		}
	}

	public List<StatisticCounterHelper> getCounters() {
		return counters;
	}

	public void setCounters(List<StatisticCounterHelper> counters) {
		this.counters = counters;
	}

	public Date getFrom() {
		return from;
	}

	public void setFrom(Date from) {
		this.from = from;
	}

	public Date getTo() {
		return to;
	}

	public void setTo(Date to) {
		this.to = to;
	}

	public List<StatisticValueHelper> getValues() {
		return values;
	}

	public void setValues(List<StatisticValueHelper> values) {
		this.values = values;
	}

	public List<StatisticValueHelper> getStaticValues() {
		return staticValues;
	}

	public void setStaticValues(List<StatisticValueHelper> staticValues) {
		this.staticValues = staticValues;
	}

	public String getDiagnosticTime() {
		return diagnosticTime;
	}

	public void setDiagnosticTime(String diagnosticTime) {
		this.diagnosticTime = diagnosticTime;
	}

	public void downloadLogFile() {
		OutputStream output;
		try {
			output = JsfUtils.getDownloadFileOutputStream("application/zip", "DoubleClueLogs.zip");
			diagnosticLogic.writeClusterLogFiles(output);
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Exception e) {
			JsfUtils.addErrorMessage("Couldn't downsload the logfiles, cause: " + e.toString());
			logger.warn(e);
		}
	}

	/* (non-Javadoc)
	 * @see com.doubleclue.dcem.core.gui.DcemView#leavingView()
	 */
	public void leavingView() {
		diagnosticTimes = null;
		statistics = null;
		values = null;
		counters = null;
		staticValues = null;
	}
}