package com.doubleclue.dcem.core.gui;

import java.io.Serializable;

import com.doubleclue.dcem.core.SubjectAbs;

@SuppressWarnings("serial")
public class ViewLink implements Serializable {
	
	SubjectAbs destSubject;
	String originVariable;
	String destinationFilterName;
	
		
	public ViewLink(SubjectAbs destSubject, String originVariable, String destinationFilterName) {
		super();
		this.destSubject = destSubject;
		this.originVariable = originVariable;
		this.destinationFilterName = destinationFilterName;
	}
	
	
	public SubjectAbs getDestSubject() {
		return destSubject;
	}
	public void setDestSubject(SubjectAbs destSubject) {
		this.destSubject = destSubject;
	}
	public String getOriginVariable() {
		return originVariable;
	}
	public void setOriginVariable(String originVariable) {
		this.originVariable = originVariable;
	}
	public String getDestinationFilterName() {
		return destinationFilterName;
	}
	public void setDestinationFilterName(String destinationFilterName) {
		this.destinationFilterName = destinationFilterName;
	}

}
