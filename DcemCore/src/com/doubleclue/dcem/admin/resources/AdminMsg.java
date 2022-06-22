package com.doubleclue.dcem.admin.resources;

import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.utils.ResourceBundleUtf8Control;

public class AdminMsg extends ResourceBundle {
	
    protected static final Control UTF8_CONTROL = new ResourceBundleUtf8Control(); 
    
    public AdminMsg () {
    	setParent(ResourceBundle.getBundle(AdminModule.RESOURCE_NAME, 
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
