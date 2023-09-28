package com.doubleclue.dcem.core.gui.converters;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.DateTimeConverter;
import javax.faces.convert.FacesConverter;

import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;

@FacesConverter(LocalDateTimeConverter.ID)
public class LocalDateTimeConverter extends DateTimeConverter {
	public static final String ID = "dcem.LocalDateTimeConverter";

	final static DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM);

	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String value) {
		Object o = super.getAsObject(facesContext, uiComponent, value);
		// NOT YET IMPLEMENTED
		throw new IllegalArgumentException(
				String.format("value=%s could not be converted to a LocalDate, result super.getAsObject=%s", value, o));
	}

	@Override
	public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object value) {
		if (value == null) {
			return super.getAsString(facesContext, uiComponent, value);
		}
		if (value instanceof Long) {
			value = DcemUtils.convertEpoch(((Long) value) * 1000);
		}
		if (value instanceof LocalDateTime) {
			LocalDateTime localDateTime = (LocalDateTime) value;
			OperatorSessionBean operator = CdiUtils.getReference(OperatorSessionBean.class);
			return operator.getUserZonedTime(localDateTime).format(dtf.withLocale(operator.getLocale()));
		} else {
			throw new IllegalArgumentException(String.format("value=%s is not a instanceof LocalDateTime", value));
		}
	}
}
