package com.doubleclue.dcem.core.exceptions;

import com.doubleclue.dcem.core.gui.JsfUtils;

/**
 * 
 * @author Emanuel Galea
 *
 */
public class DcemException extends Exception {

	private static final long serialVersionUID = 1L;

	private DcemErrorCodes errorCode;

	public String getLocalizedMessageWithMessage () {
		try {
			JsfUtils.getApplicationBundle();
			String error =  JsfUtils.getMessageFromBundle(JsfUtils.getApplicationBundle(), DcemErrorCodes.class.getSimpleName() + "." + errorCode.name(), super.getMessage());
			if (error.startsWith("???")) {
				return this.toString();
			} 
			return error;
		} catch (Exception e) {
			return this.toString();
		}
	}

	public String getLocalizedMessage() {
		try {
			String error =  JsfUtils.getStringFromBundle(DcemErrorCodes.class.getSimpleName() + "." + errorCode.name());
			if (error.startsWith("???")) {
				return this.toString();
			} 
			return error;
		} catch (Exception e) {
			return this.toString();
		}
	}
	

	public DcemException(DcemErrorCodes dcemErrorCodes, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = dcemErrorCodes;
	}

	public DcemException(DcemErrorCodes dcemErrorCodes, String message) {
		super(message, null);
		this.errorCode = dcemErrorCodes;
	}

	public DcemErrorCodes getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(DcemErrorCodes errorCode) {
		this.errorCode = errorCode;
	}

	public Throwable getExceptionCause() {
		return super.getCause();
	}

	public String getMessage() {
		return super.getMessage();
	}

	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append(errorCode.name() + " - ");
		if (getMessage() != null) {
			sb.append(getMessage());
		} 
		if (getCause() != null) {
			sb.append(" - ");
			sb.append(getCause().toString());
		}
		return sb.toString();
	}

}
