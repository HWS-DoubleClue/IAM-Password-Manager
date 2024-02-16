package com.doubleclue.dcem.core.logic;

import java.time.LocalDateTime;

import com.doubleclue.dcem.core.jpa.StatisticCounter;

public class ChartCountersData {

	LocalDateTime date;
	StatisticCounter statisticCounter;

	public ChartCountersData (LocalDateTime date, StatisticCounter statisticCounter) {
		super();
		this.date = date;
		this.statisticCounter = statisticCounter;
	}
	
	public LocalDateTime getDate() {
		return date;
	}

	public void setDate(LocalDateTime date) {
		this.date = date;
	}

	public StatisticCounter getStatisticCounter() {
		return statisticCounter;
	}

	public void setStatisticCounter(StatisticCounter statisticCounter) {
		this.statisticCounter = statisticCounter;
	}
	
}
