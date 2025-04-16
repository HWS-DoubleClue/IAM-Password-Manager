package com.doubleclue.dcem.dm.logic;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DmTimeRecurrence {
	
	public enum TimePeriods {
		Daily, Weekly, Month, Year;
	};
	
	public void initialize() {
		if (rangeFrom == null) {
			rangeFrom = LocalDate.now();
		}
		if (rangeTill == null) {
			rangeTill = LocalDate.now().plusMonths(1);
		}
		if (startAppointment == null) {
			startAppointment = LocalTime.of(8, 0);
		}
		if (endAppointment == null) {
			endAppointment = LocalTime.of(16, 0);
		}
	}
	
	@JsonProperty ("tp")
	private TimePeriods timePeriod;
	
	@JsonProperty ("sa")
	private LocalTime startAppointment;

	@JsonProperty ("ea")
	private LocalTime endAppointment;
	
	@JsonProperty ("dow")
	private List<DayOfWeek> daysOfWeek;
			
	@JsonProperty ("rf")
	private LocalDate rangeFrom;

	@JsonProperty ("rt")
	private LocalDate rangeTill;
	

	public TimePeriods getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(TimePeriods timePeriod) {
		this.timePeriod = timePeriod;
	}

	public LocalTime getStartAppointment() {
		return startAppointment;
	}

	public void setStartAppointment(LocalTime startAppointment) {
		this.startAppointment = startAppointment;
	}

	public LocalTime getEndAppointment() {
		return endAppointment;
	}

	public void setEndAppointment(LocalTime endAppointment) {
		this.endAppointment = endAppointment;
	}

	public List<DayOfWeek> getDaysOfWeek() {
		return daysOfWeek;
	}

	public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
	}
	
	public LocalDate getRangeFrom() {
		return rangeFrom;
	}

	public void setRangeFrom(LocalDate rangeFrom) {
		this.rangeFrom = rangeFrom;
	}

	public LocalDate getRangeTill() {
		return rangeTill;
	}

	public void setRangeTill(LocalDate rangeTill) {
		this.rangeTill = rangeTill;
	}

	
}
