package com.doubleclue.dcem.setup.gui;

import javax.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class DbSubject  {


	public String getModuleId() {
		return null;
	}


	public int getRank() {
		return 40;
	}	

	
	public String getIconName() {
		return "pda.png";
	}

	
	public String getPath() {
//		return "/modules/asm/devices.xhtml";
		return "dbConfig.xhtml";
	}

	
	public Class<?> getKlass() {
		return null;
	}

}
