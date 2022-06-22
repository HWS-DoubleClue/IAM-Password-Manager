package com.doubleclue.dcem.as.logic;

import com.doubleclue.dcem.core.entities.DcemReporting;

/**
 * 
 * @author Emanuel Galea
 *
 */
@SuppressWarnings("serial")
public class ExceptionReporting extends Exception {

	DcemReporting reporting;

	String errorCause;
	
	Exception exception;

	public ExceptionReporting (DcemReporting reporting,  String message) {
        super(message, null);
    	this.reporting = reporting;
    	errorCause = reporting.getErrorCode();
    }
	
	public ExceptionReporting (DcemReporting reporting,  String message, Exception exception) {
        super(message, null);
    	this.reporting = reporting;
    	errorCause = reporting.getErrorCode();
    	this.exception = exception;
    }
	
	public ExceptionReporting (String message, String errorCause) {
        super(message, null);
    	this.reporting = null;
    	this.errorCause = errorCause;
    }
	
    public ExceptionReporting (DcemReporting reporting,  String message, Throwable cause) {
        super(message, cause);
    	this.reporting = reporting;
    }
    
    public Throwable getExceptionCause() {
		return super.getCause();
	}  
	
	public String getMessage () {
		return super.getMessage();
	}

	public DcemReporting getReporting() {
		return reporting;
	}

	public void setReporting(DcemReporting reporting) {
		this.reporting = reporting;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Report: ");
		sb.append(getReporting().toString());
		sb.append("] ");
		if (getMessage() != null) {
			sb.append("[Message: ");
			sb.append(getMessage());
			sb.append("] ");
		}
		if (getCause() != null) {
			sb.append("[ Cause: ");
			sb.append(getCause().toString());
			sb.append("] ");
		}				
		return sb.toString();
	}

	public String getErrorCause() {
		return errorCause;
	}

	public void setErrorCause(String errorCause) {
		this.errorCause = errorCause;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	} 
	
	
    
    
		   
}
