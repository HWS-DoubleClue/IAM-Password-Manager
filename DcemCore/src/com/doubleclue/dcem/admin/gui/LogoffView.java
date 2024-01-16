package com.doubleclue.dcem.admin.gui;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;

@Named("logoffView")
@RequestScoped
public class LogoffView extends DcemView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public void clearCache() {
		Locale locale = Locale.ENGLISH;
		try {
			locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
		} catch (Exception e) {
			// TODO: handle exception
		}
		ResourceBundle adminResourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, locale);
		JsfUtils.addInfoMessage(adminResourceBundle.getString("browserCacheCleared"));
		PrimeFaces.current().executeScript("localStorage.clear();");
	}


	

}
