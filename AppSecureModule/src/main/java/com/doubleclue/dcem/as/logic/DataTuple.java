package com.doubleclue.dcem.as.logic;

public class DataTuple {

	private final double size;
	private final DataUnit unit;

	public DataTuple(double size, DataUnit unit) {
		this.size = size;
		this.unit = unit;
	}

	public double getSize() {
		return size;
	}

	public DataUnit getUnit() {
		return unit;
	}
}
