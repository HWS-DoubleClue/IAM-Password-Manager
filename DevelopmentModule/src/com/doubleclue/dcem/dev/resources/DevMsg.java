package com.doubleclue.dcem.dev.resources;

import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.doubleclue.dcem.dev.logic.DevModule;
import com.doubleclue.utils.ResourceBundleUtf8Control;

public class DevMsg extends ResourceBundle {
	
    protected static final Control UTF8_CONTROL = new ResourceBundleUtf8Control(); 
    
    public DevMsg () {
    	setParent(ResourceBundle.getBundle(DevModule.RESOURCE_NAME, 
                FacesContext.getCurrentInstance().getViewRoot().getLocale(), UTF8_CONTROL));
    }

	@Override
	protected Object handleGetObject(String key) {
		return parent.getObject(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		 return parent.getKeys();
	}
	
	

}
