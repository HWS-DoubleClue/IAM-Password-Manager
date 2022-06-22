package com.doubleclue.dcem.core.jpa;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
public class StatisticCounter implements Serializable {

	@JsonProperty ("c")
	public long count = 0;
	
	@JsonProperty ("et")
	public long executionTime = 0;
	
	
	@JsonProperty ("at")
	public long aveTime = 0;
	
	@JsonProperty ("mt")
	public long longestTime = 0;

	public long getCount() {
		return count;
	}
	
	public void reset () {
		count = 0;
		longestTime = 0;
		aveTime = 0;
		executionTime = 0;
	}

	public void setCount(long count) {
		this.count = count;
	}

	
	public long getAveTime() {
		if (aveTime == 0 && executionTime > 0) {
			return (executionTime / count);
		}
		return aveTime;
	}

	public void setAveTime(long aveTime) {
		this.aveTime = aveTime;
	}

	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	public long getLongestTime() {
		return longestTime;
	}

	public void setLongestTime(long longestTime) {
		this.longestTime = longestTime;
	}
	
	
	
	
}
