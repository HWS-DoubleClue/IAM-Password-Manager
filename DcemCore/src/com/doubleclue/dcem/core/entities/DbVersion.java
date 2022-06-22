package com.doubleclue.dcem.core.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the kernel_versions database table.
 * NOT IN USE
 */
@Entity
@Table(name="sys_dbversion")
public class DbVersion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="moduleId")
	private String moduleId;

	@Column(name="versionStr", length=64)
	private String versionStr;
	
	@Column(name="dbversion")
	private int version;

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getVersionStr() {
		return versionStr;
	}

	public void setVersionStr(String versionStr) {
		this.versionStr = versionStr;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "DbVersion [moduleId=" + moduleId + ", versionStr=" + versionStr + ", version=" + version + "]";
	}

	
  		
	
}