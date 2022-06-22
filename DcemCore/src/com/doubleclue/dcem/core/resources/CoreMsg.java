package com.doubleclue.dcem.core.resources;

import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.ResourceBundleUtf8Control;

public class CoreMsg extends ResourceBundle {
	
    protected static final Control UTF8_CONTROL = new ResourceBundleUtf8Control(); 
    
    public CoreMsg () {
    	setParent(ResourceBundle.getBundle(SystemModule.RESOUCE_NAME, 
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
