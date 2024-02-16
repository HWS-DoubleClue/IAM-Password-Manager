package com.doubleclue.dcem.core.gui;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;


/**
 * 
 * @author Emanuel Galea
 *
 */
@Named ("errorDisplay")
@SessionScoped
public class ErrorDisplayBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
//	private final static Logger logger = LogManager.getLogger(ErrorDisplayBean.class);
	
	private String message;

	boolean errorOn = false;

	private Set<Throwable> throwableSet = new HashSet<Throwable>();
	
	public String getMessage() {
		return message;
	}



	public boolean isErrorOn() {
		return errorOn;
	}

	public void setErrorOn(boolean errorOn) {
		this.errorOn = errorOn;
	}

	public void addThrowable(Throwable throwable) {
		throwableSet.add(throwable);
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Set<Throwable> getThrowableSet() {
		return throwableSet;
	}

	public void setThrowableSet(Set<Throwable> throwableSet) {
		this.throwableSet = throwableSet;
	}

}
