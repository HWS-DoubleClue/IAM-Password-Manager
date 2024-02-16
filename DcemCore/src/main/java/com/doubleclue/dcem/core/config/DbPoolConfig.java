package com.doubleclue.dcem.core.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * @author Emanuel Galea
 * 
 *
 */

@XmlType
@XmlRootElement (name ="dbpool")
public class DbPoolConfig implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/* Non-Guified Parameters */
	int databasePoolCheckConnectionInterval = 300;   // 5 minutes
	int databasePoolMinimum = 3;
	int databasePoolMaximum = 20;
	int databasePoolTimeout = 21600;  	// 6 Hours
	int checkoutTimeout = 10000; 		// 10 seconds
	int maxStatements = 50;
	int numHelperThreads = 4;
	int acquireIncrement = 4;
	int acquireRetryAttempts = 30;
	int acquireRetryDelay = 1000; 		// 1 second
	
	int maxIdleTimeExcessConnections = 1200;  // 20 minutes;
	
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	
	public int getDatabasePoolCheckConnectionInterval() {
		return databasePoolCheckConnectionInterval;
	}

	public void setDatabasePoolCheckConnectionInterval(int databasePoolCheckConnectionInterval) {
		this.databasePoolCheckConnectionInterval = databasePoolCheckConnectionInterval;
	}

	public int getDatabasePoolMinimum() {
		return databasePoolMinimum;
	}

	public void setDatabasePoolMinimum(int databasePoolMinimum) {
		this.databasePoolMinimum = databasePoolMinimum;
	}

	public int getDatabasePoolTimeout() {
		return databasePoolTimeout;
	}

	public void setDatabasePoolTimeout(int databasePoolTimeout) {
		this.databasePoolTimeout = databasePoolTimeout;
	}
	
	

//	@XmlTransient
//	public String getDecryptedMgtPassword() throws LogicLayerInternalErrorException {
//
//		if (encryptedMgtPassword != null && !encryptedMgtPassword.trim().isEmpty()) {
//			decryptedMgtPassword = SaltEncryption.decryptData(encryptedMgtPassword);
//		}
//
//		return decryptedMgtPassword;
//		
//	}
//
//	@XmlTransient
//	public String getDecryptedSvcPassword() throws LogicLayerInternalErrorException {
//
//		if (encryptedSvcPassword != null && !encryptedSvcPassword.trim().isEmpty()) {
//			decryptedSvcPassword = SaltEncryption.decryptData(encryptedSvcPassword);
//		}
//
//		return decryptedSvcPassword;
//		
//	}
	
	public int getCheckoutTimeout() {
		return checkoutTimeout;
	}

	public void setCheckoutTimeout(int checkoutTimeout) {
		this.checkoutTimeout = checkoutTimeout;
	}

	public int getMaxStatements() {
		return maxStatements;
	}

	public void setMaxStatements(int maxStatements) {
		this.maxStatements = maxStatements;
	}

	public int getNumHelperThreads() {
		return numHelperThreads;
	}

	public void setNumHelperThreads(int numHelperThreads) {
		this.numHelperThreads = numHelperThreads;
	}

	public int getAcquireIncrement() {
		return acquireIncrement;
	}

	public void setAcquireIncrement(int acquireIncrement) {
		this.acquireIncrement = acquireIncrement;
	}

	public int getAcquireRetryAttempts() {
		return acquireRetryAttempts;
	}

	public void setAcquireRetryAttempts(int acquireRetryAttempts) {
		this.acquireRetryAttempts = acquireRetryAttempts;
	}

	public int getAcquireRetryDelay() {
		return acquireRetryDelay;
	}

	public void setAcquireRetryDelay(int acquireRetryDelay) {
		this.acquireRetryDelay = acquireRetryDelay;
	}

	public int getMaxIdleTimeExcessConnections() {
		return maxIdleTimeExcessConnections;
	}

	public void setMaxIdleTimeExcessConnections(int maxIdleTimeExcessConnections) {
		this.maxIdleTimeExcessConnections = maxIdleTimeExcessConnections;
	}


	public int getDatabasePoolMaximum() {
		return databasePoolMaximum;
	}


	public void setDatabasePoolMaximum(int databasePoolMaximum) {
		this.databasePoolMaximum = databasePoolMaximum;
	}

}