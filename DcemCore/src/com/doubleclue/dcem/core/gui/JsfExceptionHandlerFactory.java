package com.doubleclue.dcem.core.gui;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;

public class JsfExceptionHandlerFactory extends ExceptionHandlerFactory {
	
	ExceptionHandlerFactory exceptionHandlerFactory;

	public JsfExceptionHandlerFactory(ExceptionHandlerFactory parent) {
		this.exceptionHandlerFactory = parent;
	}
   
    @Override
    public ExceptionHandler getExceptionHandler() {
        return new CustomExceptionHandler(exceptionHandlerFactory.getExceptionHandler());
    }
}


