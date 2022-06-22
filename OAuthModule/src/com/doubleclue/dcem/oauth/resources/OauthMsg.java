package com.doubleclue.dcem.oauth.resources;

import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import com.doubleclue.utils.ResourceBundleUtf8Control;

public class OauthMsg extends ResourceBundle {
	
	protected static final String BUNDLE_NAME = "com.doubleclue.dcem.oauth.resources.Messages";
    protected static final Control UTF8_CONTROL = new ResourceBundleUtf8Control(); 
    
    public OauthMsg () {
    	setParent(ResourceBundle.getBundle(BUNDLE_NAME, 
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
