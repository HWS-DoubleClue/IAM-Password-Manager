package com.doubleclue.dcem.as.logic;

import java.text.DecimalFormat;

public enum DataUnit {

	BYTE("BY"), KILOBYTE("KB"), MEGABYTE("MB"), GIGABYTE("GB"), TERABYTE("TB"), PETABYTE("PB");

	private static final DecimalFormat df = new DecimalFormat("#.##");
	private static final int multiplier = 1024;
	private final String symbol;

	private DataUnit(String value) {
		this.symbol = value;
	}

	public static DataUnit fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (DataUnit b : DataUnit.values()) {
				if (b.symbol.equalsIgnoreCase(text)) {
					return b;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return symbol;
	}

	public static DataTuple getByteCountAsTuple(long bytes) {
		if (bytes < multiplier) {
			return new DataTuple(bytes, BYTE);
		} else {
			int exp = (int) (Math.log(bytes) / Math.log(multiplier));
			return new DataTuple(bytes / Math.pow(multiplier, exp), values()[exp]);
		}
	}

	public static String getByteCountAsString(long bytes) {
		DataTuple dt = getByteCountAsTuple(bytes);
		return String.format("%s %s", df.format(dt.getSize()), dt.getUnit());
	}

	public static long getByteCount(double count, DataUnit unit) {
		return (long) (count * (Math.pow(multiplier, unit.ordinal())));
	}

	public static long getByteCount(DataTuple dt) {
		return getByteCount(dt.getSize(), dt.getUnit());
	}

	public static double convertToUnit(double value, DataUnit fromUnit, DataUnit toUnit) {
		return value * Math.pow(multiplier, toUnit.ordinal() - fromUnit.ordinal());
	}
}
