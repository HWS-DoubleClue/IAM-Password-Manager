package com.doubleclue.dcem.core.gui.converters;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.doubleclue.dcem.core.DcemConstants;

// FacesConverter("com.doubleclue.defaultConverter")
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
		Locale locale;
		DateFormat dateformat;
		
		TimeZone timeZone = TimeZone.getDefault();
		if (context != null) {
			locale = context.getViewRoot().getLocale();
		} else {
			locale = Locale.getDefault();
		}
		dateformat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, locale);
		try {
			timeZone = (TimeZone) context.getExternalContext().getSessionMap().get(DcemConstants.SESSION_TIMEZONE);
			if (timeZone != null) {
				dateformat.setTimeZone(timeZone);
			}
		} catch (Exception e) {
			
		}
		if (value.getClass() == Timestamp.class) {
			return dateformat.format(((Timestamp) value));
		}
		if (value.getClass() == java.sql.Date.class) {
			DateFormat dayDateformat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
			dayDateformat.setTimeZone(timeZone);
			return dayDateformat.format(((java.sql.Date) value));
		}
		if (value instanceof LocalDateTime) {
			ZonedDateTime zonedDateTime = ZonedDateTime.of((LocalDateTime) value, timeZone.toZoneId());
			return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(locale).withZone(timeZone.toZoneId()).format(zonedDateTime);
		}
		if (value instanceof ZonedDateTime) {
			return dateformat.format((ZonedDateTime)value);
		}
		if (value instanceof LocalDate) {
			DateFormat dayDateformat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
			dayDateformat.setTimeZone(timeZone);
			return dayDateformat.format(((LocalDate) value));
		}
		return value.toString();
	}

}
