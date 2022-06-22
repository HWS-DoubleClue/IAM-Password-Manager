package com.doubleclue.dcem.core.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;;

/**
 * @author Emanuel Galea
 */
@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class GuiFilters {

	

//	private static final Logger logger = LogManager.getLogger(ViewVariable.class);

//	@XmlElement(name = "view")
//	String viewName;
	
	
	@XmlElement(name = "items")
	ArrayList<FilterItem> filters = null;
	

	public GuiFilters() {
	}


//	public String getViewName() {
//		return viewName;
//	}
//
//
//	public void setViewName(String viewName) {
//		this.viewName = viewName;
//	}


	public List<FilterItem> getFilters() {
		return filters;
	}


	public void setFilters(ArrayList<FilterItem> filters) {
		this.filters = filters;
	}	

	

	
}
