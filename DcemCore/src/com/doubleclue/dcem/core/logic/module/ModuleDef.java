package com.doubleclue.dcem.core.logic.module;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author Joachim Frank
 *
 */
@XmlType
@XmlRootElement(name = "moduleDef")
public class ModuleDef implements Serializable {

	private static final long serialVersionUID = 1L;
	private String moduleId;
	String version; 			// Format: Major.Minor.serviceRelease
	String flavor;
	
	public ModuleDef(String moduleId, String version, String extension) {
		super();
		this.moduleId = moduleId;
		this.version = version;
		this.flavor = extension;
	}
	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getExtension() {
		return flavor;
	}
	public void setExtension(String extension) {
		this.flavor = extension;
	}
	
	
}
