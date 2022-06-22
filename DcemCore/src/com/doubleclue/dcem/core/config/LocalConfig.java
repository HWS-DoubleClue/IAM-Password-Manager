package com.doubleclue.dcem.core.config;

import java.io.Serializable;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DatabaseUtils;

@XmlType
@XmlRootElement (name ="configuration")
public class LocalConfig implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int fileFormatVersion = 0;
	
	@DecimalMin(value = "1")
	@DecimalMax(value = "10")
	private int scalabilityFactor = 4;

	private int maximumMemoryJvm = 0;
	
	boolean operatorDoubleClueAuthentication = false;
	
	String rootPassword;
	
	int maxConnections = 20000;

	DatabaseConfig database = new DatabaseConfig();
	DbPoolConfig dbPoolConfig = new DbPoolConfig();
	String nodeName = null;
	

	@Override
	public Object clone() throws CloneNotSupportedException {
		LocalConfig localConfigClone = (LocalConfig) super.clone();
		localConfigClone.database = (DatabaseConfig) localConfigClone.getDatabase().clone();
		return localConfigClone;
	}
	
	public DatabaseConfig getDatabase() {
		return database;
	}

	public void setDatabase(DatabaseConfig database) {
		this.database = database;
	}

	public int getFileFormatVersion() {
		return fileFormatVersion;
	}

	public void setFileFormatVersion(int fileFormatVersion) {
		this.fileFormatVersion = fileFormatVersion;
	}

	public int getScalabilityFactor() {
		return scalabilityFactor;
	}

	public void setScalabilityFactor(int scalabilityFactor) {
		this.scalabilityFactor = scalabilityFactor;
	}

	public int getMaximumMemoryJvm() {
		return maximumMemoryJvm;
	}

	public void setMaximumMemoryJvm(int maximumMemoryJvm) {
		this.maximumMemoryJvm = maximumMemoryJvm;
	}

	public String getRootPassword() {
		return rootPassword;
	}

	public void setRootPassword(String rootPassword) {
		this.rootPassword = rootPassword;
	}

	public DbPoolConfig getDbPoolConfig() {
		return dbPoolConfig;
	}

	public void setDbPoolConfig(DbPoolConfig dbPoolConfig) {
		this.dbPoolConfig = dbPoolConfig;
	}

	public void setDefaults() throws DcemException {
		database = new DatabaseConfig();
		String jdbcUrl = DatabaseUtils.createDatabaseUrl(database);
		database.setJdbcUrl(jdbcUrl);		
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public int getMaxConnections() {
		return maxConnections;
	}
	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}
	public boolean isOperatorDoubleClueAuthentication() {
		return operatorDoubleClueAuthentication;
	}
	public void setOperatorDoubleClueAuthentication(boolean operatorDoubleClueAuthentication) {
		this.operatorDoubleClueAuthentication = operatorDoubleClueAuthentication;
	}


}
