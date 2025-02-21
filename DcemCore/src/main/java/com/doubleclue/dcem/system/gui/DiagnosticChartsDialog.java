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

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.DcemChartData;
import com.doubleclue.dcem.core.logic.DiagnosticLogic;
import com.doubleclue.dcem.system.logic.StatisticValueHelper;

import software.xdev.chartjs.model.charts.LineChart;
import software.xdev.chartjs.model.data.LineData;
import software.xdev.chartjs.model.dataset.LineDataset;
import software.xdev.chartjs.model.options.LineOptions;
import software.xdev.chartjs.model.options.Plugins;

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

	private List<String> lineChartModelsList;
	

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

	public List<String> getLineChartModelsList() {
		if (lineChartModelsList == null) {
			lineChartModelsList = new ArrayList<>();
		}
		lineChartModelsList.addAll(getValueChart());
//		lineChartModelsList.addAll(getCounterChart());
		return lineChartModelsList;
	}

	/**
	 * @return
	 */
	private List<String> getValueChart() {
		HashSet<String> valuesSelected = new HashSet<>();
		for (StatisticValueHelper helper : diagnosticsView.getValues()) {
			if (helper.isChecked()) {
				valuesSelected.add(helper.getName());
			}
		}
		List<String> lineChartModels = new ArrayList<>();
		Map<String, List<DcemChartData>> diagnostics;
		try {
			diagnostics = diagnosticLogic.getChartValues(valuesSelected, timeFrom, timeTo);
			for (Map.Entry<String, List<DcemChartData>> entry : diagnostics.entrySet()) {
				LineChart lineChartModel = new LineChart();
				LineData chartData = new LineData();
				List<String> labels = new ArrayList<>();
				List<DcemChartData> listChartData = entry.getValue();
				HashMap<String, List<Number>> map = new HashMap<>();
				for (DcemChartData dcemChartData : listChartData) {
					labels.add(localDateTimeToString(dcemChartData.getDate()));
					for (Entry<String, Number> mapEntry : dcemChartData.getMap().entrySet()) {
						List<Number> lineNumbers = map.get(mapEntry.getKey());
						if (lineNumbers == null) {
							lineNumbers = new ArrayList<Number>();
							lineNumbers.add(mapEntry.getValue());
							map.put(mapEntry.getKey(), lineNumbers);
						} else {
							lineNumbers.add(mapEntry.getValue());
						}
					}
				}
				int i = 0;
				for (Entry<String, List<Number>> mapEntry : map.entrySet()) {
					LineDataset dataSet = new LineDataset();
					dataSet.setLabel(mapEntry.getKey());
					dataSet.setData(mapEntry.getValue());
					dataSet.setBorderColor(chartColor[i & 0x01]);
					chartData.addDataset(dataSet);
					i++;
				}
				chartData.setLabels(labels);
				LineOptions options = new LineOptions();
				options.setPlugins(new Plugins()
                        .setTitle(new software.xdev.chartjs.model.options.Title()
                                .setDisplay(true)
                                .setText(entry.getKey())));
				lineChartModel.setOptions(options);
				lineChartModel.setData(chartData);
				lineChartModels.add(lineChartModel.toJson());
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

	

	public void setLineChartModelsList(List<String> lineChartModelsList) {
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