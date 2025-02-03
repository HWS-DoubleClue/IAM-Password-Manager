package com.doubleclue.dcem.core.gui;

import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;

import com.doubleclue.dcem.core.DcemConstants;

public class CustomExceptionHandler extends ExceptionHandlerWrapper {

	private ExceptionHandler exceptionHandler;

	private static final Logger logger = LogManager.getLogger(CustomExceptionHandler.class);

	public CustomExceptionHandler(ExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	public ExceptionHandler getWrapped() {
		return this.exceptionHandler;
	}

	public void handle() throws FacesException {

		final Iterator<ExceptionQueuedEvent> queue = getUnhandledExceptionQueuedEvents().iterator();
		while (queue.hasNext()) {
			ExceptionQueuedEvent item = queue.next();
			ExceptionQueuedEventContext exceptionQueuedEventContext = (ExceptionQueuedEventContext) item.getSource();
			FacesContext context = FacesContext.getCurrentInstance();
			HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
			try {
				Throwable throwable = exceptionQueuedEventContext.getException();
				if (getRootCauseThrowable(throwable, null) instanceof InvalidCipherTextException) {
					request.getSession().setAttribute(DcemConstants.DOWNLOAD_CIPHER_EXCEPTION, true);
					// System.out.println("CustomExceptionHandler.handle() Encryption Error");
				} else {
					if (throwable instanceof IllegalStateException) {
						logger.debug("JSF Exception " + throwable.toString());
					} else if (throwable instanceof javax.el.ELException) {
						logger.warn("JSF Exception " + throwable.toString());
						request.getSession().setMaxInactiveInterval(1);
					} else {
						logger.debug("JSF Exception ", throwable);
						// Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
						// NavigationHandler nav = context.getApplication().getNavigationHandler();
						// requestMap.put("error-message", throwable.getMessage());
						// request.getSession().invalidate();
						// nav.handleNavigation(context, null, "ERROR");
						// context.renderResponse();
					}
				}
			} finally {
				queue.remove();
			}
		}
	}

	private Throwable getRootCauseThrowable(Throwable exception, String findExpStartWith) {
		Throwable cause = exception;
		while (cause.getCause() != null) {
			cause = cause.getCause();
			if (findExpStartWith != null) {
				if (cause.getClass().getSimpleName().startsWith(findExpStartWith)) {
					return cause;
				}
			}
		}
		if (findExpStartWith != null) {
			return null;
		}
		return cause;
	}

}
