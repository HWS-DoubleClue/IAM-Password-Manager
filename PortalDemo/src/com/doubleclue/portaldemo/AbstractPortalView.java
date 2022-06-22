package com.doubleclue.portaldemo;

import java.io.Serializable;

@SuppressWarnings("serial")
abstract public class AbstractPortalView implements Serializable {

	abstract public String getName();
	
	abstract public String getPath();
	
	

}
