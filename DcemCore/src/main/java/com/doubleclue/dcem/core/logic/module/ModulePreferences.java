package com.doubleclue.dcem.core.logic.module;

import java.io.Serializable;


public abstract class ModulePreferences implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int version;


	public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
	
	
	public int getVersion() {
		return version;
	}


	public void setVersion(int version) {
		this.version = version;
	}


	public void incrementVersion() {
		version++;
	}

	

}
