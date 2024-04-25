package com.doubleclue.dcem.core.utils.compare;

/**
 * 
 * @author Emanuel Galea
 *
 */
public class CompareException extends Exception {

	private static final long serialVersionUID = 1L;

	public CompareException(String message, Throwable cause) {
		super(message, cause);
	}

	public CompareException(String message) {
		super(message, null);
	}


	public Throwable getExceptionCause() {
		return super.getCause();
	}

	public String getMessage() {
		return super.getMessage();
	}

	public String toString () {
		StringBuffer sb = new StringBuffer();
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
