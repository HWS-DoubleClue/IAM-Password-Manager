package com.doubleclue.dcem.core.logic.module;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlRootElement(name = "module")
public class ModuleDbVersion implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	String type;
	String version;

	public ModuleDbVersion() {}
	
	public ModuleDbVersion(String type, String version) {
		setType(type);
		setVersion(version);
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	public String toString() {
		return "[type=" + type + "; version=" + version + "]";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ModuleDbVersion other = (ModuleDbVersion) obj;
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		return true;
	}
}
