package com.doubleclue.dcem.as.resources;

import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.utils.ResourceBundleUtf8Control;

public class AsMsg extends ResourceBundle {
	
    protected static final Control UTF8_CONTROL = new ResourceBundleUtf8Control(); 
    
    public AsMsg () {
    	setParent(ResourceBundle.getBundle(AsModule.RESOURCE_NAME, 
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
