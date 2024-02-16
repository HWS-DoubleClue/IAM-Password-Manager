package com.doubleclue.dcem.as.tasks;

import com.doubleclue.dcem.as.entities.ActivationCodeEntity;

public class AsCreateChangeTenantCallResult {

	private Exception exception;
	private ActivationCodeEntity activationCode;

	public AsCreateChangeTenantCallResult() {
	}

	public AsCreateChangeTenantCallResult(Exception exception, ActivationCodeEntity activationCode) {
		this.exception = exception;
		this.activationCode = activationCode;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public ActivationCodeEntity getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(ActivationCodeEntity activationCode) {
		this.activationCode = activationCode;
	}

}
