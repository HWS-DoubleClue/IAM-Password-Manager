package com.doubleclue.dcem.core.jpa;

import java.util.Comparator;

public class FilterOrderCompare implements Comparator<FilterOrder>  {

	@Override
	public int compare(FilterOrder arg0, FilterOrder arg1) {
		return arg0.getRank() - (arg1.getRank());
	}

}
