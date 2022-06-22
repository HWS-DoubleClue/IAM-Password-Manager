package com.doubleclue.dcem.core.logic;

import java.util.Date;

import com.doubleclue.dcem.core.jpa.StatisticCounter;

public class ChartCountersData {

	Date date;
	StatisticCounter statisticCounter;

	public ChartCountersData (Date date, StatisticCounter statisticCounter) {
		super();
		this.date = date;
		this.statisticCounter = statisticCounter;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public StatisticCounter getStatisticCounter() {
		return statisticCounter;
	}

	public void setStatisticCounter(StatisticCounter statisticCounter) {
		this.statisticCounter = statisticCounter;
	}
	
}
