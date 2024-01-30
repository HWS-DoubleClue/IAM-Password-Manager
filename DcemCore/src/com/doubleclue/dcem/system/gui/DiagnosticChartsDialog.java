package com.doubleclue.dcem.system.gui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.SlideEndEvent;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.line.LineChartDataSet;
import org.primefaces.model.charts.line.LineChartModel;
import org.primefaces.model.charts.line.LineChartOptions;
import org.primefaces.model.charts.optionconfig.title.Title;

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.DcemChartData;
import com.doubleclue.dcem.core.logic.DiagnosticLogic;
import com.doubleclue.dcem.system.logic.StatisticValueHelper;

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
	
	String [] chartColor = new String [] {"rgb(175, 192, 92)","rgb(75, 192, 192)" };
	
	

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
		Map<String, List<DcemChartData>> diagnostics;
		try {
			
			diagnostics = diagnosticLogic.getChartValues(valuesSelected, timeFrom, timeTo);
			for (Map.Entry<String, List<DcemChartData>> entry : diagnostics.entrySet()) {
				LineChartModel lineChartModel = new LineChartModel();
				ChartData chartData = new ChartData();
				List<String> labels = new ArrayList<>();
				List<DcemChartData> listChartData = entry.getValue();
				HashMap<String, List<Object>> map = new HashMap<>();
				for (DcemChartData dcemChartData : listChartData) {
					labels.add(localDateTimeToString(dcemChartData.getDate()));
					for (Entry<String, Number> mapEntry : dcemChartData.getMap().entrySet()) {
						List<Object> lineNumbers = map.get(mapEntry.getKey());
						if (lineNumbers == null) {
							lineNumbers = new ArrayList<Object>();
							lineNumbers.add(mapEntry.getValue());
							map.put(mapEntry.getKey(), lineNumbers);
						} else {
							lineNumbers.add(mapEntry.getValue());
						}
					}
				}
				int i = 0;
				for (Entry<String, List<Object>> mapEntry : map.entrySet()) {
					LineChartDataSet dataSet = new LineChartDataSet();
					dataSet.setLabel(mapEntry.getKey());
					dataSet.setData(mapEntry.getValue());
					dataSet.setBorderColor(chartColor[i/2]);
					chartData.addChartDataSet(dataSet);
					i++;
				}
				chartData.setLabels(labels);
				LineChartOptions options = new LineChartOptions();
				Title title = new Title();
				title.setDisplay(true);
				title.setText(entry.getKey());
				options.setTitle(title);
				lineChartModel.setOptions(options);
				lineChartModel.setData(chartData);
				lineChartModels.add(lineChartModel);
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
		return lineChartModels;
	}

	private String localDateTimeToString(LocalDateTime localDateTime) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT).withLocale(JsfUtils.getLocale());
		return localDateTime.format(dateTimeFormatter);
	}

	private List<LineChartModel> getCounterChart() {

		List<LineChartModel> list = new ArrayList<>();
		// List<String> countersSelected = new ArrayList<>();
		// for (StatisticCounterHelper helper : diagnosticsView.getCounters()) {
		// if (helper.isChecked()) {
		// countersSelected.add(helper.getName());
		// }
		// }
		// Map<String, List<ChartCountersData>> diagnostics;
		// try {
		// diagnostics = diagnosticLogic.getChartCounters(countersSelected, "", timeFrom, timeTo);
		// for (Map.Entry<String, List<ChartCountersData>> entry : diagnostics.entrySet()) {
		// LineChartModel model = new LineChartModel();
		// ChartSeries chartSeriesCount = new ChartSeries();
		// // ChartSeries chartSeriesExTime = new ChartSeries();
		// ChartSeries chartSeriesAverage = new ChartSeries();
		// ChartSeries chartSeriesLongest = new ChartSeries();
		// chartSeriesCount.setLabel("Count");
		// // chartSeriesExTime.setLabel("Execution Time");
		// chartSeriesAverage.setLabel("Average Time");
		// chartSeriesLongest.setLabel("Longest Time");
		// List<ChartCountersData> listChartCountersData = entry.getValue();
		//
		// for (ChartCountersData chartCountersData : entry.getValue()) {
		// chartSeriesCount.set(chartCountersData.getDate().toString(), chartCountersData.getStatisticCounter().getCount());
		// // chartSeriesExTime.set(entry1.getKey().toString(),
		// // entry1.getValue().getExecutionTime());
		// chartSeriesAverage.set(chartCountersData.getDate().toString(), chartCountersData.getStatisticCounter().getAveTime());
		// chartSeriesLongest.set(chartCountersData.getDate().toString(), chartCountersData.getStatisticCounter().getLongestTime());
		// }
		// model.addSeries(chartSeriesCount);
		// // model.addSeries(chartSeriesExTime);
		// model.addSeries(chartSeriesAverage);
		// model.addSeries(chartSeriesLongest);
		// model.setTitle(entry.getKey());
		// model.setZoom(true);
		// model.setShowPointLabels(true);
		// model.setLegendPosition("e");
		// model.getAxes().put(AxisType.X, new CategoryAxis(""));
		// model.getAxis(AxisType.Y);
		// Axis yAxis = model.getAxis(AxisType.Y);
		// yAxis.setLabel("");
		//
		// DateAxis xAxis = new DateAxis("");
		// xAxis.setTickAngle(-50);
		// xAxis.setMin(listChartCountersData.get(0).getDate().toString());
		// xAxis.setMax(listChartCountersData.get(listChartCountersData.size() - 1).getDate().toString());
		// xAxis.setTickFormat("%m-%#d %H:%M");
		// model.getAxes().put(AxisType.X, xAxis);
		// lineChartModelsList.add(model);
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
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