package com.doubleclue.portaldemo.gui;


import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.doubleclue.portaldemo.AbstractPortalView;

@SuppressWarnings("serial")
@Named("entryCodeView")
@SessionScoped
public class EntryCodeView extends AbstractPortalView {

	String name;
	
	
	public void actionLogin() {
		
		
	}
	

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setName(String name) {
		this.name = name;
	}		
			
}
