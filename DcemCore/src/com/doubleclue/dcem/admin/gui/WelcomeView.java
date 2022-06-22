package com.doubleclue.dcem.admin.gui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.ItemSelectEvent;
import org.primefaces.model.SortOrder;
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
import com.doubleclue.dcem.core.jpa.FilterItem;
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

	private ResourceBundle resourceBundle;

	static String[] actions = new String[] { DcemConstants.ACTION_VIEW };
	private BarChartModel userActivityBarChart;
	private PieChartModel authMethodsPieChart;

	private SelectedFormat selectedDateFormat = SelectedFormat.MONTH;
	private Date currentDate = new Date();

	public enum Action {
		NEXT, PREVIOUS
	};

	public enum SelectedFormat {
		DAY, MONTH, YEAR;
	};

	public Date getCurrentDate() {
		return currentDate;
	}

	public void setCurrentDate(Date currentDate) {
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
		if (selectedDateFormat == SelectedFormat.DAY) {
			return new SimpleDateFormat("dd MMM", operatorSessionBean.getLocale()).format(currentDate);
		} else if (selectedDateFormat == SelectedFormat.YEAR) {
			return new SimpleDateFormat("YYYY", operatorSessionBean.getLocale()).format(currentDate);
		} else {
			return new SimpleDateFormat("MMM YYYY", operatorSessionBean.getLocale()).format(currentDate);
		}
	}

	public void itemSelect(ItemSelectEvent event) {
		ViewVariable viewVariable = reportingView.getDisplayViewVariables().get(0);
		viewVariable.setFilterOperator(FilterOperator.BETWEEN);
		
		Calendar calStart = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		
		calStart.setTime(currentDate);
		calEnd.setTime(currentDate);
		
		calStart.set(Calendar.MINUTE, 0);
		calStart.set(Calendar.SECOND, 0);
		
		calEnd.set(Calendar.MINUTE, 59);
		calEnd.set(Calendar.SECOND, 59);
		
		Integer value = event.getItemIndex();
		switch (selectedDateFormat) {
		case DAY:
			calStart.set(Calendar.HOUR_OF_DAY, value);
			calEnd.set(Calendar.HOUR_OF_DAY, value);
			break;
		case MONTH:
			calStart.set(Calendar.DATE, value + 1);
			calStart.set(Calendar.HOUR_OF_DAY, 0);
			calEnd.set(Calendar.DATE, value + 1);	
			calEnd.set(Calendar.HOUR_OF_DAY, 23);
			break;
		case YEAR:
			calStart.set(Calendar.MONTH, value);
			calStart.set(Calendar.DATE, 1);
			calStart.set(Calendar.HOUR_OF_DAY, 0);
			calEnd.set(Calendar.DATE, calStart.getActualMaximum(Calendar.DAY_OF_MONTH));
			calEnd.set(Calendar.MONTH, calStart.get(Calendar.MONTH));
			calEnd.set(Calendar.HOUR_OF_DAY, 23);
			break;
		default:
			break;
		}
		
		viewVariable.setFilterValue(calStart.getTime());
		viewVariable.setFilterToValue(calEnd.getTime());
		ViewVariable viewVariable2 = reportingView.getDisplayViewVariable(DcemReporting_.action.getName());
		viewVariable2.setFilterValue(Integer.toString(ReportAction.Authenticate.ordinal()));
		viewVariable2.setFilterOperator(FilterOperator.EQUALS);
		DcemModule dcemModule = viewNavigator.getActiveModule();
		viewNavigator.setActiveView(dcemModule.getId()  + DcemConstants.MODULE_VIEW_SPLITTER + "reportingView");
	}

	@PostConstruct
	private void init() {
		subject = welcomeSubject;
		resourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	private void setUpCharts() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		selectedDateFormat = SelectedFormat.MONTH;
		userActivityBarChart = dashboardLogic.getUserActivityBarChart(cal.getTime(), SelectedFormat.MONTH, resourceBundle);
		authMethodsPieChart = dashboardLogic.getAuthMethodsPieChart(cal.getTime(), SelectedFormat.MONTH, resourceBundle);
	}

	@Override
	public void triggerAction(AutoViewAction autoViewAction) {
		System.out.println("WelcomeView.triggerAction()");
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
		DcemAction dcemAction = new DcemAction(subject, DcemConstants.ACTION_DELETE);
		DcemAction dcemActionManage = new DcemAction(subject, DcemConstants.ACTION_MANAGE);
		return operatorSessionBean.isPermission(dcemActionManage, dcemAction);
	}

	public void changeDateSelection(SelectedFormat format) {
		selectedDateFormat = format;
		currentDate = new Date();
		userActivityBarChart = dashboardLogic.getUserActivityBarChart(currentDate, format, resourceBundle);
		authMethodsPieChart = dashboardLogic.getAuthMethodsPieChart(currentDate, format, resourceBundle);
	}

	public void changeFormatSelection(Action selectedAction) {
		if (currentDate == null) {
			currentDate = new Date();
		}
		Date startDate = null;
		SelectedFormat selectedFormat = getSelectedDateFormat();
		Calendar cal = null;

		if (selectedFormat == SelectedFormat.DAY) { // per hour
			cal = Calendar.getInstance();
			cal.setTime(currentDate);
			if (selectedAction == Action.PREVIOUS)
				cal.add(Calendar.DAY_OF_YEAR, -1);
			else if (selectedAction == Action.NEXT) {
				cal.add(Calendar.DAY_OF_YEAR, +1);
			}
			startDate = cal.getTime();
		}

		if (selectedFormat == SelectedFormat.MONTH) { // per day
			cal = Calendar.getInstance();
			cal.setTime(currentDate);
			if (selectedAction == Action.PREVIOUS)
				cal.add(Calendar.MONTH, -1);
			else if (selectedAction == Action.NEXT) {
				cal.add(Calendar.MONTH, +1);
			}
			cal.set(Calendar.DAY_OF_MONTH, 1);

			startDate = cal.getTime();
		}

		if (selectedFormat == SelectedFormat.YEAR) { // per month
			cal = Calendar.getInstance();
			cal.setTime(currentDate);
			if (selectedAction == Action.PREVIOUS)
				cal.add(Calendar.YEAR, -1);
			else if (selectedAction == Action.NEXT) {
				cal.add(Calendar.YEAR, +1);
			}

			int year = cal.get(Calendar.YEAR);
			cal.set(year, 01, 01);
			startDate = cal.getTime();
		}

		currentDate = startDate;
		userActivityBarChart = dashboardLogic.getUserActivityBarChart(currentDate, selectedFormat, resourceBundle);
		authMethodsPieChart = dashboardLogic.getAuthMethodsPieChart(currentDate, selectedFormat, resourceBundle);
	}

	public static Date getLastDateOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}

	public boolean isLastDate() {
		Calendar today = Calendar.getInstance();
		Calendar selectedDate = Calendar.getInstance();
		selectedDate.setTime(currentDate);

		if (selectedDateFormat == SelectedFormat.DAY) {
			selectedDate.add(Calendar.DATE, 1);
		} else if (selectedDateFormat == SelectedFormat.MONTH) {
			selectedDate.add(Calendar.MONTH, 1);
		} else if (selectedDateFormat == SelectedFormat.YEAR) {
			selectedDate.add(Calendar.YEAR, 1);
		}

		if (selectedDate.after(today)) {
			return true;
		}
		return false;
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
}
