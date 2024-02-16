package com.doubleclue.dcem.core.gui.converters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("com.doubleclue.EpochToDate")
public class EpochToDate implements Converter {
	
	DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return value;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null || (Integer)value == 0) {
			return "";
		}
	    return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(new Date (((Integer)value) * 1000));
	}

}
