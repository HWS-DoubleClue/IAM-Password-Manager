package com.doubleclue.dcem.as.comm.client;

import com.doubleclue.comm.thrift.AppException;

/**
 * 
 * @author Emanuel Galea
 *
 */
@SuppressWarnings("serial")
public class AsException extends Exception {


	private AsErrorCodes errorCode;

	public AsException(AsErrorCodes errorCode, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = errorCode;
	}

	public AsException(AsErrorCodes errorCode, String message) {
		super(message, null);
		this.errorCode = errorCode;
	}

	public AsErrorCodes getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(AsErrorCodes errorCode) {
		this.errorCode = errorCode;
	}

	public String getSecondErrorCode() {
		if (errorCode == AsErrorCodes.SERVER_RESPONSE_ERROR) {
			if (getCause() != null && getCause() instanceof AppException) {
				return ((AppException) getCause()).getError();
			} else {
				return null;
			}
		}
		return null;
	}

	public Throwable getExceptionCause() {
		return super.getCause();
	}

	public String getMessage() {
		return super.getMessage();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(errorCode.name());
		if (super.getMessage() != null) {
			sb.append("-");
			sb.append(super.getMessage());
		}
		if (super.getCause() != null) {
			sb.append(" / ");
			sb.append(super.getCause());
		}
		return sb.toString();
	}

}
