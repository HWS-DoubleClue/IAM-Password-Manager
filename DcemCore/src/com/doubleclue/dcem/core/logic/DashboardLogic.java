package com.doubleclue.dcem.core.logic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.PieChartModel;

import com.doubleclue.dcem.admin.gui.WelcomeView.SelectedFormat;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;

@ApplicationScoped
@Named("dashboardLogic")
public class DashboardLogic {

	private static Logger logger = LogManager.getLogger(DashboardLogic.class);
	private static final String RESOURCE_PREFIX = "dashboardLogic.";

	AsModuleApi asModuleApi;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@PostConstruct
	public void init() {
		asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
	}

	public enum SelectedReportAction {
		PASSWORD(
				"Password",
				17),
		SMS("Sms", 18),
		VOICE("Voice", 19),
		OTP("OTP", 20),
		MOTP("MOTP", 21),
		PUSH("Push", 22),
		QRCODE("Qr-code", 23),
		FIDO("Fido", 24);

		private String label;
		private int value;

		private SelectedReportAction(String label, int value) {
			this.value = value;
			this.label = label;
		}

		public String getLabel() {
			return label;
		}

		public int getValue() {
			return value;
		}
	};

	public BarChartModel getUserActivityBarChart(Date startDate, SelectedFormat selectedDateFormat, ResourceBundle resourceBundle) {
		try {
			BarChartModel userActivityBarChart = new BarChartModel();

			HashMap<Date, Long> userActivityList = asModuleApi.getUserActivityData(startDate, selectedDateFormat, true);

			HashMap<Date, Long> userFailedAuthenticationList = asModuleApi.getUserActivityData(startDate, selectedDateFormat, false);

			ChartSeries seriesUserActivity = getBarSeries(userActivityList, JsfUtils.getStringSafely(resourceBundle, RESOURCE_PREFIX + "successfulLogins"),
					selectedDateFormat);
			ChartSeries seriesUserFailedAuthentication = getBarSeries(userFailedAuthenticationList,
					JsfUtils.getStringSafely(resourceBundle, RESOURCE_PREFIX + "failedLogins"), selectedDateFormat);

			userActivityBarChart.addSeries(seriesUserActivity);
			userActivityBarChart.addSeries(seriesUserFailedAuthentication);
			userActivityBarChart.setTitle(JsfUtils.getStringSafely(resourceBundle, RESOURCE_PREFIX + "userLoginActivity"));
			userActivityBarChart.setZoom(true);
			userActivityBarChart.getAxis(AxisType.Y).setLabel(JsfUtils.getStringSafely(resourceBundle, RESOURCE_PREFIX + "noOfUserLogins"));
			CategoryAxis axis = new CategoryAxis();
			axis.setTickAngle(-50);
			userActivityBarChart.getAxes().put(AxisType.X, axis);
			userActivityBarChart.setLegendPosition("e");
			Axis yAxis = userActivityBarChart.getAxis(AxisType.Y);
			yAxis.setMin(0);
			userActivityBarChart.setBarPadding(2);
			userActivityBarChart.setBarMargin(5);;
			userActivityBarChart.setSeriesColors("005078, d24141");
			userActivityBarChart.setAnimate(true);
			userActivityBarChart.setDatatipFormat("%2$.2f");
			return userActivityBarChart;
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, RESOURCE_PREFIX + "barChartFailed");
			logger.warn("Dashboard Bar chart Filed to load. ", exp);
			return new BarChartModel();
		}
	}

	private ChartSeries getBarSeries(HashMap<Date, Long> userActivityList, String label, SelectedFormat selectedDateFormat) {
		DateFormat dateFormat;
		ChartSeries series = new ChartSeries();
		series.setLabel(label);
		Map<Date, Long> sortedMap = new TreeMap<Date, Long>(userActivityList);
		if (selectedDateFormat == SelectedFormat.DAY) {
			dateFormat = new SimpleDateFormat("HH:mm", operatorSessionBean.getLocale());
		} else if (selectedDateFormat == SelectedFormat.YEAR) {
			dateFormat = new SimpleDateFormat("MMM", operatorSessionBean.getLocale());
		} else {
			dateFormat = new SimpleDateFormat("dd");
		}
		for (Entry<Date, Long> set : sortedMap.entrySet()) {
			series.set(dateFormat.format(set.getKey()), set.getValue());
		}
		return series;
	}

	public PieChartModel getAuthMethodsPieChart(Date startDate, SelectedFormat format, ResourceBundle resourceBundle) {
		AsModuleApi asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
		if (asModuleApi != null) {
			try {
				PieChartModel authMethodsPieChart = new PieChartModel();

				HashMap<Integer, Long> authMethodActivityList = asModuleApi.getAuthMethodActivityData(startDate, format);
				for (Entry<Integer, Long> set : authMethodActivityList.entrySet()) {
					if (set.getKey() == SelectedReportAction.PASSWORD.getValue()) {
						authMethodsPieChart.set(SelectedReportAction.PASSWORD.getLabel(), set.getValue());
					} else if (set.getKey() == SelectedReportAction.SMS.getValue()) {
						authMethodsPieChart.set(SelectedReportAction.SMS.getLabel(), set.getValue());
					} else if (set.getKey() == SelectedReportAction.VOICE.getValue()) {
						authMethodsPieChart.set(SelectedReportAction.VOICE.getLabel(), set.getValue());
					} else if (set.getKey() == SelectedReportAction.OTP.getValue()) {
						authMethodsPieChart.set(SelectedReportAction.OTP.getLabel(), set.getValue());
					} else if (set.getKey() == SelectedReportAction.MOTP.getValue()) {
						authMethodsPieChart.set(SelectedReportAction.MOTP.getLabel(), set.getValue());
					} else if (set.getKey() == SelectedReportAction.PUSH.getValue()) {
						authMethodsPieChart.set(SelectedReportAction.PUSH.getLabel(), set.getValue());
					} else if (set.getKey() == SelectedReportAction.QRCODE.getValue()) {
						authMethodsPieChart.set(SelectedReportAction.QRCODE.getLabel(), set.getValue());
					} else if (set.getKey() == SelectedReportAction.FIDO.getValue()) {
						authMethodsPieChart.set(SelectedReportAction.FIDO.getLabel(), set.getValue());
					}
				}

				authMethodsPieChart.setTitle(JsfUtils.getStringSafely(resourceBundle, RESOURCE_PREFIX + "authMethods"));
				authMethodsPieChart.setLegendPosition("e");
				authMethodsPieChart.setFill(true);
				authMethodsPieChart.setShowDataLabels(true);
				authMethodsPieChart.setDiameter(150);
				authMethodsPieChart.setShadow(false);

				return authMethodsPieChart;
			} catch (Exception exp) {
				JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, RESOURCE_PREFIX + "pieChartFailed");
				logger.warn("Dashboard Pie chart Filed to load. ", exp);
				return new PieChartModel();
			}
		}
		return new PieChartModel();
	}
}
