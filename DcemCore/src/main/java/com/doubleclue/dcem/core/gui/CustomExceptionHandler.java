package com.doubleclue.dcem.core.gui;

import java.util.Iterator;
import java.util.Map;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
		 	HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			try {
				Throwable throwable = exceptionQueuedEventContext.getException();
				if (getRootCauseThrowable(throwable, null) instanceof InvalidCipherTextException) {
					request.getSession().setAttribute(DcemConstants.DOWNLOAD_CIPHER_EXCEPTION, true);
					// System.out.println("CustomExceptionHandler.handle() Encryption Error");
				} else {
					if (throwable instanceof IllegalStateException) {
						logger.debug("JSF Exception " + throwable.toString());

					} else if (throwable instanceof org.primefaces.csp.CspException) {
						logger.debug("CSPException " + throwable.toString());
						// } else if (throwable instanceof javax.el.ELException) {
						// logger.warn("JSF Exception " + throwable.toString());
						// request.getSession().setMaxInactiveInterval(2);
						// //request.getSession().invalidate();
						// try {
						// context.getApplication().getNavigationHandler().handleNavigation(fc, null, "/dcem/error_.xhtml");
						// context.renderResponse();
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
					} else {
						logger.error("JSF Exception ", throwable);
 						request.setAttribute("error-message", throwable);
						try {
							 NavigationHandler navigationHandler = facesContext.getApplication().getNavigationHandler();
							 navigationHandler.handleNavigation(facesContext, null, "/error_.xhtml");
							 facesContext.renderResponse();
//							response.sendRedirect("/dcem/DcemExceptionHandler");
//							response.setStatus(9000);
//							facesContext.responseComplete();
//							 System.out.println("CustomExceptionHandler.handle() " + throwable.toString());
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}
			} finally {
				queue.remove();
			}
		}
		getWrapped().handle();
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
