package com.doubleclue.dcem.admin.gui;

import java.awt.Color;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.ItemSelectEvent;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.subjects.WelcomeSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;

import software.xdev.chartjs.model.charts.BarChart;
import software.xdev.chartjs.model.data.BarData;
import software.xdev.chartjs.model.dataset.BarDataset;
import software.xdev.chartjs.model.options.BarOptions;
import software.xdev.chartjs.model.options.Plugins;
import software.xdev.chartjs.model.options.Title;
import software.xdev.chartjs.model.options.scale.Scales;

@SuppressWarnings("serial")
@Named("welcomeView")
@SessionScoped
public class WelcomeView extends DcemView {

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	WelcomeSubject welcomeSubject;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	ReportingView reportingView;

	@Inject
	DcemReportingLogic dcemReportingLogic;

	@Inject
	UserDialogBean userDialog;

	@Inject
	UserPasswordDialog userPasswordDialog;

	@Inject
	UserLogic userLogic;

	private static final String RESOURCE_PREFIX = "dashboardLogic.";

	private ResourceBundle resourceBundle;

	private String userActivityBarModel;

	private SelectedFormat selectedDateFormat = SelectedFormat.MONTH;
	private LocalDate currentDate = LocalDate.now();

	public enum Action {
		NEXT, PREVIOUS
	};

	public enum SelectedFormat {
		DAY, MONTH, YEAR;
	};

	@PostConstruct
	private void init() {
		subject = welcomeSubject;
		resourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	public LocalDate getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(LocalDate currentDate) {
		this.currentDate = currentDate;
	}

	public SelectedFormat getSelectedDateFormat() {
		return selectedDateFormat;
	}

	public void setSelectedDateFormat(SelectedFormat selectedDateFormat) {
		this.selectedDateFormat = selectedDateFormat;
	}

	public SelectedFormat[] getDateFormats() {
		return SelectedFormat.values();
	}

	public String getChosenDate() {
		DateTimeFormatter dateTimeFormatter;
		if (selectedDateFormat == SelectedFormat.DAY) {
			return currentDate.format(DateTimeFormatter.ofPattern("dd MMM", operatorSessionBean.getLocale()));
		} else if (selectedDateFormat == SelectedFormat.YEAR) {
			return currentDate.format(DateTimeFormatter.ofPattern("YYY", operatorSessionBean.getLocale()));
		} else {
			return currentDate.format(DateTimeFormatter.ofPattern("MMM YYYY", operatorSessionBean.getLocale()));
		}
	}

	public void itemSelect(ItemSelectEvent event) {
		LocalDateTime start;
		LocalDateTime end;
		Integer value = event.getItemIndex();
		switch (selectedDateFormat) {
		case MONTH:
			start = currentDate.withDayOfMonth(value).atTime(LocalTime.MIN);
			end = currentDate.withDayOfMonth(value + 1).atTime(LocalTime.MAX);
			break;
		case YEAR:
			start = currentDate.withMonth(value).atTime(LocalTime.MIN);
			end = currentDate.withMonth(value + 1).atTime(LocalTime.MAX);
			break;
		default: // DAY
			start = currentDate.atTime(value, 0);
			end = currentDate.atTime(value + 1, 0);
			break;
		}
		ViewVariable viewVariable = reportingView.getDisplayViewVariables().get(0);
		viewVariable.setFilterOperator(FilterOperator.BETWEEN);
		List<LocalDateTime> listValues = new ArrayList<>();
		listValues.add(start);
		listValues.add(end);
		viewVariable.setFilterValue(listValues);
		DcemModule dcemModule = viewNavigator.getActiveModule();
		viewNavigator.setActiveView(dcemModule.getId() + DcemConstants.MODULE_VIEW_SPLITTER + "reportingView");
	}

	private void setUpCharts() {
		selectedDateFormat = SelectedFormat.MONTH;
		userActivityBarModel = getUserActivityBarChart(currentDate.atStartOfDay(), SelectedFormat.MONTH, resourceBundle);
	}

	@Override
	public void triggerAction(AutoViewAction autoViewAction) {
		super.triggerAction(autoViewAction);
	}

	public String getWelcomeText() {
		return JsfUtils.getMessageFromBundle(AdminModule.RESOURCE_NAME, "view.Welcome.Text", operatorSessionBean.getDcemUser().getDisplayNameOrLoginId(),
				operatorSessionBean.getRolesText(), DcemCluster.getInstance().getDcemNode().getName());
	}

	public boolean isPrivilegedForDeletingAlerts() {
		return operatorSessionBean.isPermission(new DcemAction(subject, DcemConstants.ACTION_DELETE));
	}

	public boolean isPermissionView() {
		return operatorSessionBean.isPermission(welcomeSubject.getDcemActions());
	}

	public void changeDateSelection(SelectedFormat format) {
		selectedDateFormat = format;
		currentDate = LocalDate.now();
		userActivityBarModel = getUserActivityBarChart(currentDate.atStartOfDay(), format, resourceBundle);
	}

	public void changeFormatSelection(Action selectedAction) {
		if (currentDate == null) {
			currentDate = LocalDate.now();
		}
		SelectedFormat selectedFormat = getSelectedDateFormat();
		switch (selectedDateFormat) {
		default:
		case DAY:
			if (selectedAction == Action.PREVIOUS)
				currentDate = currentDate.minusDays(1);
			else if (selectedAction == Action.NEXT) {
				currentDate = currentDate.plusDays(1);
			}
			break;
		case MONTH:
			if (selectedAction == Action.PREVIOUS)
				currentDate = currentDate.minusMonths(1);
			else if (selectedAction == Action.NEXT) {
				currentDate = currentDate.plusMonths(1);
			}
			break;
		case YEAR:
			if (selectedAction == Action.PREVIOUS)
				currentDate = currentDate.minusYears(1);
			else if (selectedAction == Action.NEXT) {
				currentDate = currentDate.plusYears(1);
			}
			break;
		}
		userActivityBarModel = getUserActivityBarChart(currentDate.atStartOfDay(), selectedFormat, resourceBundle);
	}

	private String getUserActivityBarChart(LocalDateTime startDate, SelectedFormat selectedDateFormat, ResourceBundle resourceBundle) {
		try {
			HashMap<LocalDateTime, Long> userActivityList = dcemReportingLogic.getUserActivityData(startDate, selectedDateFormat, true);
			HashMap<LocalDateTime, Long> userFailedAuthenticationList = dcemReportingLogic.getUserActivityData(startDate, selectedDateFormat, false);
			BarChart barChart = new BarChart();
			barChart.setOptions(new BarOptions().setResponsive(true).setMaintainAspectRatio(false));

			BarData barData = new BarData();
			BarDataset barDatasetLogin = new BarDataset();
			barDatasetLogin.setBackgroundColor("rgb(30, 166, 224)");
			barDatasetLogin.setLabel(JsfUtils.getStringSafely(resourceBundle, RESOURCE_PREFIX + "successfulLogins"));
			
			BarDataset barDatasetFailed = new BarDataset();
			barDatasetFailed.setBackgroundColor("rgb(255, 166, 124)");
			barDatasetFailed.setLabel(JsfUtils.getStringSafely(resourceBundle, RESOURCE_PREFIX + "noOfUserLogins"));
			addChartDataSet (barData, barDatasetLogin, userActivityList, selectedDateFormat);	
			addChartDataSet (barData, barDatasetFailed, userFailedAuthenticationList, selectedDateFormat);	
			barChart.setData(barData);
			return barChart.toJson();
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, RESOURCE_PREFIX + "barChartFailed");
			logger.warn("Dashboard Bar chart Filed to load. ", exp);
			return null;
		}
	}

	private void addChartDataSet(BarData barData, BarDataset barDataset, HashMap<LocalDateTime, Long> userActivityList,
			SelectedFormat selectedDateFormat) {
		DateTimeFormatter dateTimeFormatter;
		List<Number> values = new ArrayList<>();
		List<String> labels = new ArrayList<>();

		Map<LocalDateTime, Long> sortedMap = new TreeMap<LocalDateTime, Long>(userActivityList);
		if (selectedDateFormat == SelectedFormat.DAY) {
			dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", operatorSessionBean.getLocale());
		} else if (selectedDateFormat == SelectedFormat.YEAR) {
			dateTimeFormatter = DateTimeFormatter.ofPattern("MMM", operatorSessionBean.getLocale());
		} else {
			dateTimeFormatter = DateTimeFormatter.ofPattern("dd", operatorSessionBean.getLocale());
		}
		for (Entry<LocalDateTime, Long> set : sortedMap.entrySet()) {
			labels.add(set.getKey().format(dateTimeFormatter));
			values.add(set.getValue());
		}
		barData.setLabels(labels);
		barDataset.setData(values);
		barDataset.setData(values);
		barData.addDataset(barDataset);
		return;
	}

	public void leavingView() {
		userActivityBarModel = null;
	}

	@Override
	public void reload() {
		setUpCharts();
		super.reload();
	}

	public String getUserActivityBarModel() {
		if (userActivityBarModel == null) {
			setUpCharts();
		}
		return userActivityBarModel;
	}

	public void setUserActivityBarModel(String userActivityBarModel) {
		this.userActivityBarModel = userActivityBarModel;
	}
}
