package com.doubleclue.dcem.radius.resources;

import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.doubleclue.dcem.radius.logic.RadiusModule;
import com.doubleclue.utils.ResourceBundleUtf8Control;

public class RadiusMsg extends ResourceBundle {
	
    protected static final Control UTF8_CONTROL = new ResourceBundleUtf8Control(); 
    
    public RadiusMsg () {
    	setParent(ResourceBundle.getBundle(RadiusModule.RESOUCE_NAME, 
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
