package com.doubleclue.dcem.core.gui.converters;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.doubleclue.dcem.core.DcemConstants;

@FacesConverter("com.doubleclue.defaultConverter")
public class DefaultConvertor implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		// System.out.println("EpochToDate.getAsObject");
		return value;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value == null) {
			return "";
		}
		if (value.getClass() == Timestamp.class) {
			Locale locale = Locale.getDefault();
			if (context != null) {
				locale = context.getViewRoot().getLocale();
			}
			DateFormat dateformat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
			try {
				TimeZone timezone = (TimeZone) context.getExternalContext().getSessionMap().get(DcemConstants.SESSION_TIMEZONE);
				dateformat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT, locale);
				if (timezone != null) {
					dateformat.setTimeZone(timezone);
				}
			} catch (Exception e) {
				
			}
			return dateformat.format(((Timestamp) value));
		}
		if (value.getClass() == java.sql.Date.class) {
			Locale locale = Locale.getDefault();
			if (context != null) {
				locale = context.getViewRoot().getLocale();
			}
			DateFormat dateformat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
			return dateformat.format(((Date) value));
		}
		if (value instanceof LocalDateTime) {
			Locale locale = Locale.getDefault();
			if (context != null) {
				locale = context.getViewRoot().getLocale();
			}
			return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(locale).format((LocalDateTime)value);
		}
		if (value instanceof LocalDate) {
			Locale locale = Locale.getDefault();
			if (context != null) {
				locale = context.getViewRoot().getLocale();
			}
			return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(((LocalDate)value));
		}
		return value.toString();
	}

}
