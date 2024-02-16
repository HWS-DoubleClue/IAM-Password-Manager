package com.doubleclue.dcem.core.config;

import java.io.Serializable;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.jpa.DatabaseTypes;

/**
 * @author Emanuel Galea
 * 
 *
 */

@XmlType
@XmlRootElement(name = "database")
public class DatabaseConfig implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_HOST_NAME = "localhost";
	public static final String DEFAULT_DATABASE_NAME = "dcem_db";
	public static final String DEFAULT_USER_NAME = "root";
	public static final String DEFAULT_SCHEMA_NAME = "dbo";

	private boolean databaseConfigured = false;

	@NotNull(message = "{database.config.databaseType.notNull}")
	private String databaseType = DatabaseTypes.DERBY.name();

	@NotNull(message = "{database.config.databaseName.notNull}")
	@Pattern(regexp = "[0-9a-zA-Z_]*", message = "{database.config.databaseName.invalid}")
	private String databaseName = DEFAULT_DATABASE_NAME;

	@NotNull(message = "{database.config.ipAddress.notNull}")
	private String ipAddress = DEFAULT_HOST_NAME;

	@NotNull(message = "{database.config.port.notNull}")
	@DecimalMin(value = "1", message = "{database.config.port.invalid}")
	@DecimalMax(value = "65535", message = "{database.config.port.invalid}")
	@Digits(fraction = 0, integer = 5, message = "{database.config.port.invalid}")
	private int port = DatabaseTypes.MARIADB.getDefaultPort();


	private String jdbcUrl = DatabaseTypes.DERBY.getProtocol();

	@NotNull(message = "{database.config.adminName.notNull}")
	private String adminName = DEFAULT_USER_NAME;

	private String databaseEncryptionKey;

	@Size(min = 0, max = 64, message = "{database.config.adminPassword.invalid}")
	@XmlTransient
	private String adminPassword;
	
	private String adminPasswordEnc;
	
	

	private String schemaName = DEFAULT_SCHEMA_NAME;

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		this.databaseType = databaseType;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getDatabaseEncryptionKey() {
		return databaseEncryptionKey;
	}

	public void setDatabaseEncryptionKey(String databaseEncryptionKey) {
		this.databaseEncryptionKey = databaseEncryptionKey;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	@XmlTransient
	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public boolean isDatabaseConfigured() {
		return databaseConfigured;
	}

	public void setDatabaseConfigured(boolean databaseConfigured) {
		this.databaseConfigured = databaseConfigured;
	}

	
	// @XmlTransient
	// public String getDecryptedMgtPassword() throws
	// LogicLayerInternalErrorException {
	//
	// if (encryptedMgtPassword != null &&
	// !encryptedMgtPassword.trim().isEmpty()) {
	// decryptedMgtPassword = SaltEncryption.decryptData(encryptedMgtPassword);
	// }
	//
	// return decryptedMgtPassword;
	//
	// }
	//
	// @XmlTransient
	// public String getDecryptedSvcPassword() throws
	// LogicLayerInternalErrorException {
	//
	// if (encryptedSvcPassword != null &&
	// !encryptedSvcPassword.trim().isEmpty()) {
	// decryptedSvcPassword = SaltEncryption.decryptData(encryptedSvcPassword);
	// }
	//
	// return decryptedSvcPassword;
	//
	// }

	
	public String getAdminPasswordEnc() {
		return adminPasswordEnc;
	}

	public void setAdminPasswordEnc(String adminPasswordEnc) {
		this.adminPasswordEnc = adminPasswordEnc;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
}