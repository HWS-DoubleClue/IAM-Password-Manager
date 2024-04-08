package com.doubleclue.dcem.core.gui.converters;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;

@FacesConverter(value = "DcemUserConvertor", managed = true)
public class DcemUserConvertor implements Converter<DcemUser> {

	@Inject
	UserLogic userLogic;

	@Override
	public DcemUser getAsObject(FacesContext context, UIComponent component, String value) {
		if (value != null && value.trim().length() > 0) {
			try {
				UserLogic userLogic = CdiUtils.getReference(UserLogic.class);
				return userLogic.getUser(Integer.parseInt(value));
			} catch (Exception e) {
				throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error: Not a valid user: " + value, "Not a valid user."));
			}
		} else {
			return null;
		}

	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, DcemUser value) {
		if (value == null) {
			return "";
		}
		return value.getId().toString();
	}

}
