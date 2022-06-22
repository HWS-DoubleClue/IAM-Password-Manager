package com.doubleclue.dcem.core.logic;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChartData {

	Date date;
	Map<String, Number> map;

//	public ChartData(Date date, Map<String, Number> map) {
//		super();
//		this.date = date;
//		this.map = map;
//	}

	public ChartData(Date date, String node, Number number) {
		super();
		this.date = date;
		this.map = new HashMap<>();
		map.put(node, number);
	}
	
	public void addNodeNumber (String node, Number number) {
		map.put(node,  number);
	}

	public Map<String, Number> getMap() {
		return map;
	}

	public void setMap(Map<String, Number> map) {
		this.map = map;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
