package com.doubleclue.dcem.radius.preferences;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@SuppressWarnings("serial")
@XmlType
@XmlRootElement(name = "radiusPreferences")
public class RadiusPreferences extends ModulePreferences {




	@DcemGui(help = "If enabled, all traffic data will be saved in the log file.")
	boolean traceData = false;

	@DcemGui
	boolean writeReportForValidAuthentication;
	
	@DcemGui(separator = "Archive - Archiving is executed on the 1st day of each month", help = "days. Reports older than this are archived automatically. Set to '0' to turn off automatic archiving.")
	int durationForReportArchive = 356;


	public boolean isTraceData() {
		return traceData;
	}

	public void setTraceData(boolean traceData) {
		this.traceData = traceData;
	}

	
	public boolean isWriteReportForValidAuthentication() {
		return writeReportForValidAuthentication;
	}

	public void setWriteReportForValidAuthentication(boolean writeReportForValidAuthentication) {
		this.writeReportForValidAuthentication = writeReportForValidAuthentication;
	}

	public int getDurationForReportArchive() {
		return durationForReportArchive;
	}

	public void setDurationForReportArchive(int durationForReportArchive) {
		this.durationForReportArchive = durationForReportArchive;
	}

}
