package com.doubleclue.dcem.core.gui.converters;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.TimeZone;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.FacesConverter;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.utils.DcemUtils;

@FacesConverter(LocalDateTimeConverter.ID)
public class LocalDateTimeConverter extends DateTimeConverter {
    public static final String ID = "dcem.LocalDateTimeConverter";
    
    final static DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
        Object o = super.getAsObject(facesContext, uiComponent, value);
   // NOT YET IMPLEMENTED
        if (o == null) {
            return null;
        }

        if (o instanceof Date) {
            Instant instant = Instant.ofEpochMilli(((Date) o).getTime());
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
        } else {
            throw new IllegalArgumentException(String.format("value=%s could not be converted to a LocalDate, result super.getAsObject=%s", value, o));
        }
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
        if (value == null) {
            return super.getAsString(facesContext, uiComponent,value);
        }
        if (value instanceof Long) {
        	value = DcemUtils.convertEpoch(((Long)value) * 1000);
        }
        if (value instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) value;
            TimeZone timeZone = (TimeZone) facesContext.getExternalContext().getSessionMap().get(DcemConstants.SESSION_TIMEZONE);
            if (TimeZone.getDefault().equals(timeZone) == false) {
            	ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, timeZone.toZoneId());
        		localDateTime = zonedDateTime.withZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime();
    		}            
            return localDateTime.format(dtf);
        } else {
            throw new IllegalArgumentException(String.format("value=%s is not a instanceof LocalDateTime", value));
        }
    }
}