package com.doubleclue.dcem.admin.gui;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.PieChartModel;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.admin.subjects.WelcomeSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemReporting_;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.logic.DashboardLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.module.DcemModule;

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
	DashboardLogic dashboardLogic;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	ReportingView reportingView;
	
	@Inject
	UserDialogBean userDialog;

	private ResourceBundle resourceBundle;

	static String[] actions = new String[] { DcemConstants.ACTION_VIEW };
	private BarChartModel userActivityBarChart;
	private PieChartModel authMethodsPieChart;

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
		addAutoViewAction(DcemConstants.ACTION_USER_PROFILE, resourceBundle, userDialog, DcemConstants.USER_PROFILE_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_CHANGE_PASSWORD, resourceBundle, userDialog, DcemConstants.CHANGE_PASSWORD_DIALOG);
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
		DateTimeFormatter  dateTimeFormatter;
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
		userActivityBarChart = dashboardLogic.getUserActivityBarChart(currentDate.atStartOfDay(), SelectedFormat.MONTH, resourceBundle);
		authMethodsPieChart = dashboardLogic.getAuthMethodsPieChart(currentDate.atStartOfDay(), SelectedFormat.MONTH, resourceBundle);
	}

	@Override
	public void triggerAction(AutoViewAction autoViewAction) {
		super.triggerAction(autoViewAction);
	}

	public String getWelcomeText() {
		return JsfUtils.getMessageFromBundle(AdminModule.RESOURCE_NAME, "view.Welcome.Text", operatorSessionBean.getDcemUser().getDisplayNameOrLoginId(),
				operatorSessionBean.getRolesText(), DcemCluster.getInstance().getDcemNode().getName());
	}

	public BarChartModel getUserActivityBarChart() {
		if (userActivityBarChart == null) {
			setUpCharts();
		}
		return userActivityBarChart;
	}

	public PieChartModel getAuthMethodsPieChart() {
		if (authMethodsPieChart == null) {
			setUpCharts();
		}
		return authMethodsPieChart;
	}

	public boolean isPrivilegedForDeletingAlerts() {
		return operatorSessionBean.isPermission(new DcemAction(subject, DcemConstants.ACTION_DELETE));
	}

	public void changeDateSelection(SelectedFormat format) {
		selectedDateFormat = format;
		currentDate = LocalDate.now();
		userActivityBarChart = dashboardLogic.getUserActivityBarChart(currentDate.atStartOfDay(), format, resourceBundle);
		authMethodsPieChart = dashboardLogic.getAuthMethodsPieChart(currentDate.atStartOfDay(), format, resourceBundle);
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
		userActivityBarChart = dashboardLogic.getUserActivityBarChart(currentDate.atStartOfDay(), selectedFormat, resourceBundle);
		authMethodsPieChart = dashboardLogic.getAuthMethodsPieChart(currentDate.atStartOfDay(), selectedFormat, resourceBundle);
	}

	

	public void leavingView() {
		userActivityBarChart = null;
		authMethodsPieChart = null;
	}

	@Override
	public void reload() {
		setUpCharts();
		super.reload();
	}
	
	public void editProfile () {
		List<Object> certificates = new ArrayList<Object>();
		certificates.add(operatorSessionBean.getDcemUser());
		autoViewBean.setSelectedItems(certificates);
		viewNavigator.setActiveDialog(this.getAutoViewAction(DcemConstants.ACTION_USER_PROFILE));
	}
	
}
