package com.doubleclue.dcem.core.jpa;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.primefaces.model.SortOrder;;

/**
 * @author Emanuel Galea
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "FilterItem")
@XmlAccessorType (XmlAccessType.FIELD)
public class FilterItem implements Serializable {

//	private static final Logger logger = LogManager.getLogger(ViewVariable.class);

	String id;
	Object filterValue;
	Object filterToValue;
	FilterOperator filterOperator = FilterOperator.NONE;
	int sortRank;
	SortOrder sortOrder;

	public FilterItem() {
	}	

	/**
	 * @param id
	 * @param filterValue
	 * @param filterToValue
	 * @param filterOperator
	 * @param sortRank
	 * @param sortOrder
	 */
	public FilterItem(String id, Object filterValue, Object filterToValue, FilterOperator filterOperator, int sortRank, SortOrder sortOrder) {
		super();
		this.id = id;
		this.filterValue = filterValue;
		this.filterToValue = filterToValue;
		this.filterOperator = filterOperator;
		this.sortRank = sortRank;
		this.sortOrder = sortOrder;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Object getFilterValue() {
		return filterValue;
	}
	public void setFilterValue(Object filterValue) {
		this.filterValue = filterValue;
	}

	public Object getFilterToValue() {
		return filterToValue;
	}

	public void setFilterToValue(Object filterToValue) {
		this.filterToValue = filterToValue;
	}

	public FilterOperator getFilterOperator() {
		return filterOperator;
	}

	public void setFilterOperator(FilterOperator filterOperator) {
		this.filterOperator = filterOperator;
	}
	public int getSortRank() {
		return sortRank;
	}

	public void setSortRank(int sortRank) {
		this.sortRank = sortRank;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	public String toString() {
		return "Filter: (Value=" + filterValue + ", Sort=" + sortOrder + ", rank=" + sortRank + ", Operator=" + filterOperator + ") ";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilterItem other = (FilterItem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
