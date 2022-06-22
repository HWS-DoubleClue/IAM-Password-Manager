//#excludeif COMMUNITY_EDITION == true
package com.doubleclue.dcem.system.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.SlideEndEvent;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.DateAxis;
import org.primefaces.model.chart.LineChartModel;

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.logic.ChartCountersData;
import com.doubleclue.dcem.core.logic.ChartData;
import com.doubleclue.dcem.core.logic.DiagnosticLogic;
import com.doubleclue.dcem.system.logic.StatisticCounterHelper;
import com.doubleclue.dcem.system.logic.StatisticValueHelper;
import com.doubleclue.dcem.system.logic.SystemModule;

@Named("diagnosticsChartsDialog")
@SessionScoped
public class DiagnosticChartsDialog extends DcemDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private DiagnosticLogic diagnosticLogic;

	@Inject
	private DiagnosticsView diagnosticsView;

	private List<LineChartModel> lineChartModelsList;

	private String timeFrom;

	private String timeTo;

	private int timeFromNum = 10;

	private int timeToNum = 40;

	@Inject
	private SystemModule systemmodule;

	// @Override
	// public int getHeight() {
	// return 0;
	// }
	//
	// @Override
	// public String getWidth() {
	// return null;
	// }

	static final String COUNTER_COUNT = "Count";
	static final String COUNTER_AVERAGE_TIME = "Average Time";
	static final String COUNTER_TIME = "Longest Time";

	public List<LineChartModel> getLineChartModelsList() {
		if (lineChartModelsList == null) {
			lineChartModelsList = new ArrayList<>();
		}
		lineChartModelsList.addAll(getValueChart());
		lineChartModelsList.addAll(getCounterChart());
		return lineChartModelsList;
	}

	/**
	 * @return
	 */
	private List<LineChartModel> getValueChart() {
		HashSet<String> valuesSelected = new HashSet<>();
		for (StatisticValueHelper helper : diagnosticsView.getValues()) {
			if (helper.isChecked()) {
				valuesSelected.add(helper.getName());
			}
		}
		List<LineChartModel> lineChartModels = new ArrayList<>();
		Map<String, List<ChartData>> diagnostics;
		try {
			diagnostics = diagnosticLogic.getChartValues(valuesSelected, timeFrom, timeTo);
			for (Map.Entry<String, List<ChartData>> entry : diagnostics.entrySet()) {
				LineChartModel model = new LineChartModel();

				List<ChartData> listChartData = entry.getValue();
				ChartSeries chartSeries;
				HashMap<String, ChartSeries> mapSeries = new HashMap<>();

				for (ChartData chartData : listChartData) {

					for (Map.Entry<String, Number> entry2 : chartData.getMap().entrySet()) {
						chartSeries = mapSeries.get(entry2.getKey());
						if (chartSeries == null) {
							chartSeries = new ChartSeries();
							chartSeries.setLabel(entry2.getKey());
							mapSeries.put(entry2.getKey(), chartSeries);
							model.addSeries(chartSeries);
						}
						chartSeries.set(chartData.getDate().toString(), entry2.getValue());
					}
				}
				model.setTitle(entry.getKey());
				model.setShowPointLabels(true);
				model.setZoom(true);

				model.getAxis(AxisType.Y);
				Axis yAxis = model.getAxis(AxisType.Y);
				yAxis.setLabel("");

				DateAxis xAxis = new DateAxis("");
				xAxis.setTickAngle(-50);
				xAxis.setMin(listChartData.get(0).getDate().toString());
				xAxis.setMax(listChartData.get(listChartData.size() - 1).getDate().toString());
				xAxis.setTickFormat("%m-%#d %H:%M");
				model.getAxes().put(AxisType.X, xAxis);

				lineChartModels.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return lineChartModels;
	}

	private List<LineChartModel> getCounterChart() {

		List<LineChartModel> list = new ArrayList<>();
		List<String> countersSelected = new ArrayList<>();
		for (StatisticCounterHelper helper : diagnosticsView.getCounters()) {
			if (helper.isChecked()) {
				countersSelected.add(helper.getName());
			}
		}
		Map<String, List<ChartCountersData>> diagnostics;
		try {
			diagnostics = diagnosticLogic.getChartCounters(countersSelected, "", timeFrom, timeTo);
			for (Map.Entry<String, List<ChartCountersData>> entry : diagnostics.entrySet()) {
				LineChartModel model = new LineChartModel();
				ChartSeries chartSeriesCount = new ChartSeries();
				// ChartSeries chartSeriesExTime = new ChartSeries();
				ChartSeries chartSeriesAverage = new ChartSeries();
				ChartSeries chartSeriesLongest = new ChartSeries();
				chartSeriesCount.setLabel("Count");
				// chartSeriesExTime.setLabel("Execution Time");
				chartSeriesAverage.setLabel("Average Time");
				chartSeriesLongest.setLabel("Longest Time");
				List<ChartCountersData> listChartCountersData = entry.getValue();

				for (ChartCountersData chartCountersData : entry.getValue()) {
					chartSeriesCount.set(chartCountersData.getDate().toString(), chartCountersData.getStatisticCounter().getCount());
					// chartSeriesExTime.set(entry1.getKey().toString(),
					// entry1.getValue().getExecutionTime());
					chartSeriesAverage.set(chartCountersData.getDate().toString(), chartCountersData.getStatisticCounter().getAveTime());
					chartSeriesLongest.set(chartCountersData.getDate().toString(), chartCountersData.getStatisticCounter().getLongestTime());
				}
				model.addSeries(chartSeriesCount);
				// model.addSeries(chartSeriesExTime);
				model.addSeries(chartSeriesAverage);
				model.addSeries(chartSeriesLongest);
				model.setTitle(entry.getKey());
				model.setZoom(true);
				model.setShowPointLabels(true);
				model.setLegendPosition("e");
				model.getAxes().put(AxisType.X, new CategoryAxis(""));
				model.getAxis(AxisType.Y);
				Axis yAxis = model.getAxis(AxisType.Y);
				yAxis.setLabel("");

				DateAxis xAxis = new DateAxis("");
				xAxis.setTickAngle(-50);
				xAxis.setMin(listChartCountersData.get(0).getDate().toString());
				xAxis.setMax(listChartCountersData.get(listChartCountersData.size() - 1).getDate().toString());
				xAxis.setTickFormat("%m-%#d %H:%M");
				model.getAxes().put(AxisType.X, xAxis);
				lineChartModelsList.add(model);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public void setLineChartModelsList(List<LineChartModel> lineChartModelsList) {
		this.lineChartModelsList = lineChartModelsList;
	}

	public String getTimeFrom() {
		return timeFrom;
	}

	public void setTimeFrom(String timeFrom) {
		this.timeFrom = timeFrom;
	}

	public String getTimeTo() {
		return timeTo;
	}

	public void setTimeTo(String timeTo) {
		this.timeTo = timeTo;
	}

	public void onSlideEnd(SlideEndEvent event) {
		lineChartModelsList = null;
	}

	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		List<SelectItem> diagnosticTimes = diagnosticsView.getDiagnosticTimes();
		if (diagnosticTimes.size() < 2) {
			return;
		}
		timeToNum = diagnosticTimes.size() - 1;
		timeFromNum = 1;
		if (timeToNum > 100) {
			timeFromNum = (timeToNum / 100) * 80;
		}
		timeTo = diagnosticTimes.get(timeToNum).getLabel();

		timeFrom = diagnosticTimes.get(timeFromNum).getLabel();
		lineChartModelsList = null;
	}

	public int getTimeFromNum() {
		return timeFromNum;
	}

	public void setTimeFromNum(int timeFromNum) {
		lineChartModelsList = null;
		if (timeFromNum == 0) {
			timeFromNum = 1;
		}
		this.timeFromNum = timeFromNum;
		timeFrom = diagnosticsView.getDiagnosticTimes().get(timeFromNum).getLabel();
	}

	public int getTimeToNum() {
		return timeToNum;
	}

	public void setTimeToNum(int timeToNum) {
		lineChartModelsList = null;
		this.timeToNum = timeToNum;
		timeTo = diagnosticsView.getDiagnosticTimes().get(timeToNum).getLabel();

	}

	public int getMaxValue() {
		return diagnosticsView.getDiagnosticTimes().size() - 1;
	}
	
	public String getWidth() {
		return "1024";
	}

	/* (non-Javadoc)
	 * @see com.doubleclue.dcem.core.gui.DcemDialog#leavingDialog()
	 */
	public void leavingDialog() {
		lineChartModelsList = null;
	}

}