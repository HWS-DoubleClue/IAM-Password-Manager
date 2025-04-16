package com.doubleclue.dcem.dm.gui;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import javax.inject.Inject;
import javax.inject.Named;
import com.doubleclue.dcem.as.entities.CloudSafeTagEntity;
import com.doubleclue.dcem.as.logic.CloudSafeTagLogic;

@Named
@ApplicationScoped
@FacesConverter(value = "tagEntityConverter", managed = true)
public class TagEntityConverter implements Converter<CloudSafeTagEntity> {

	@Inject
	private CloudSafeTagLogic cloudSafeTagLogic;

	@Override
	public CloudSafeTagEntity getAsObject(FacesContext context, UIComponent component, String value) {
		if (value != null && value.trim().length() > 0) {
			try {
				return cloudSafeTagLogic.getTagById(Integer.parseInt(value));
			} catch (NumberFormatException e) {
				throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid country."));
			}
		} else {
			return null;
		}
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, CloudSafeTagEntity cloudSafeTagEntity) {
		if (cloudSafeTagEntity != null) {
			return String.valueOf(cloudSafeTagEntity.getId());
		} else {
			return null;
		}
	}
}
