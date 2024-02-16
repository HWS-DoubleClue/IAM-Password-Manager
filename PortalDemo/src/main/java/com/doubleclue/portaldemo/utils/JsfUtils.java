package com.doubleclue.portaldemo.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * General useful static utilies for workign with JSF.
 * 
 * @author Duncan Mills
 * @author Steve Muench $Id: JSFUtils.java,v 1.6 2006/04/20 12:07:44 steve Exp $
 *
 */
public class JsfUtils {

	/**
	 * Convenience method for getting application attribute.
	 * 
	 * @param name
	 *            attribute name
	 * @return attribute value
	 */
	public static Object getFromApplication(String name) {
		return getExternalContext().getApplicationMap().get(name);
	}

	/**
	 * Convenience method for setting application attribute.
	 * 
	 * @param name
	 *            attribute name
	 * @param value
	 *            attribute value
	 */
	public static void storeOnApplication(String name, Object value) {
		getExternalContext().getApplicationMap().put(name, value);
	}

	// /*
	// * Convenience method for getting HTTP Request attribute.
	// * @param name attribute name
	// * @return attribute value
	// */
	// public static Object getFromRequest(String name) {
	// return getExternalContext().getRequestMap().get(name);
	// }

	/**
	 * Convenience method for setting HTTP Request attribute.
	 * 
	 * @param name
	 *            attribute name
	 * @param value
	 *            attribute value
	 */
	public static void storeOnRequest(String name, Object value) {
		getExternalContext().getRequestMap().put(name, value);
	}

	// /**
	// * Convenience method for setting Session variables.
	// * @param key object key
	// * @param object value to store
	// */
	// public static void storeOnSession(String key, Object object) {
	// FacesContext ctx = getFacesContext();
	// Map sessionState = ctx.getExternalContext().getSessionMap();
	// sessionState.put(key, object);
	// }

	// /**
	// * Convenience method for getting Session variables.
	// * @param key object key
	// * @return session object for key
	// */
	// public static Object getFromSession(String key) {
	// FacesContext ctx = getFacesContext();
	// Map sessionState = ctx.getExternalContext().getSessionMap();
	// return sessionState.get(key);
	// }

	/**
	 * Convenience method for removing HTTP session attribute.
	 * 
	 * @param name
	 *            attribute name
	 * @return attribute value
	 */
	public static Object removeFromSession(String name) {
		return getExternalContext().getSessionMap().remove(name);
	}

	/**
	 * Get & remove an HTTP session attribute.
	 * 
	 * @param name
	 *            attribute name
	 * @return attribute value
	 */
	public static Object getAndRemoveFromSession(String name) {
		Map<String, Object> sessionMap = getExternalContext().getSessionMap();
		Object returnObject = sessionMap.get(name);
		sessionMap.remove(name);
		return returnObject;
	}

	/**
	 * Returns ExternalContext from FacesContext
	 * 
	 * @return
	 */
	public static ExternalContext getExternalContext() {
		return getFacesContext().getExternalContext();
	}

	/**
	 * Returns HttpServletRequest from FacesContext
	 * 
	 * @return
	 */
	public static HttpServletRequest getHttpServletRequest() {
		return (HttpServletRequest) getExternalContext().getRequest();
	}

	/**
	 * Returns Logged in User's IP Address
	 * 
	 * @return
	 */
	public static String getRemoteHost() {
		return getHttpServletRequest().getRemoteHost();
	}

	/**
	 * Returns logged-in user id
	 * 
	 * @return
	 */
	public static String getLoggedInUserId() {
		return getHttpServletRequest().getRemoteUser();
	}

	/*
	 * Internal method to pull out the correct local message bundle
	 */
	public static ResourceBundle getBundle(String pMessageBundleName) {
		Locale locale = getLocale();
		ClassLoader ldr = Thread.currentThread().getContextClassLoader();
		return ResourceBundle.getBundle(pMessageBundleName, locale, ldr);
	}

	public static Locale getLocale() {
		FacesContext ctx = getFacesContext();
		UIViewRoot uiRoot = ctx.getViewRoot();
		return uiRoot.getLocale();
	}

	/**
	 * This method will be used mainly if you are giving your project as a jar
	 * to other applications like task-flow project.
	 * 
	 * @param pMessageBundleName
	 * @param pMessageId
	 * @param pParametersValue
	 */
	public static void addInformationMessage(String pMessageBundleName, String pMessageId, Object... pParametersValue) {
		addMessage(FacesMessage.SEVERITY_INFO, pMessageBundleName, pMessageId, pParametersValue, null);
	}

	/**
	 * This method will be used mainly if you are giving your project as a jar
	 * to other applications like task-flow project.
	 * 
	 * @param pMessageBundleName
	 * @param pMessageId
	 * @param pParametersValue
	 */
	public static void addWarningMessage(String pMessageBundleName, String pMessageId, Object... pParametersValue) {
		addMessage(FacesMessage.SEVERITY_WARN, pMessageBundleName, pMessageId, pParametersValue, null);
	}

	/**
	 * This method will be used mainly if you are giving your project as a jar
	 * to other applications like task-flow project.
	 * 
	 * @param pMessageBundleName
	 * @param pMessageId
	 * @param pParametersValue
	 */
	public static void addErrorMessage(String pMessageBundleName, String pMessageId, Object... pParametersValue) {

		addMessage(FacesMessage.SEVERITY_ERROR, pMessageBundleName, pMessageId, pParametersValue, null);
	}

	/**
	 * Adding page level error message
	 * 
	 * @param argMessage
	 */
	public static void addErrorMessage(String argMessage) {
		addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR, argMessage, argMessage), null);
	}
	
	public static void addInfoMessage(String argMessage) {
		addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, argMessage, argMessage), null);
	}
	
	public static void addWarnMessage(String argMessage) {
		addMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, argMessage, argMessage), null);
	}

	/**
	 * This method will be used mainly if you are giving your project as a jar
	 * to other applications like task-flow project.
	 * 
	 * @param pMessageBundleName
	 * @param pMessageId
	 * @param pParametersValue
	 * @param pUIComponent
	 */
	public static void addErrorMessageToComponent(String pMessageBundleName, String pMessageId,
			Object[] pParametersValue, UIComponent pUIComponent) {
		addMessage(FacesMessage.SEVERITY_ERROR, pMessageBundleName, pMessageId, pParametersValue, pUIComponent);
	}

	public static void addErrorMessageToComponentId(String message, String clientId) {
		FacesMessage facesMessage = new FacesMessage(message, message);
		facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
		getFacesContext().addMessage(clientId, facesMessage);

	}

	/**
	 * @param pSeverity
	 * @param pMessageBundleName
	 * @param pMessageId
	 * @param pParametersValue
	 * @param pUIComponent
	 */
	private static void addMessage(FacesMessage.Severity pSeverity, String pMessageBundleName, String pMessageId,
			Object[] pParametersValue, UIComponent pUIComponent) {
		FacesMessage facesMessage = MessageBundleHandler.getInstance().getMessage(pSeverity, pMessageBundleName,
				pMessageId, pParametersValue, pUIComponent);

		addMessage(facesMessage, pUIComponent);
	}

	/**
	 * @param argFacesMessage
	 * @param argUIComponent
	 */
	private static void addMessage(FacesMessage argFacesMessage, UIComponent argUIComponent) {
		FacesContext facesContext = getFacesContext();
		// if client id is null then add as global message
		if (argUIComponent != null) {
			facesContext.addMessage(argUIComponent.getClientId(facesContext), argFacesMessage);
		} else {
			facesContext.addMessage(null, argFacesMessage);
		}
	}

	public static Object getLabel(UIComponent component) {
		Object o = null;
		if (component != null) {
			o = component.getAttributes().get("label");
			if (o == null)
				o = component.getValueExpression("label");
		}
		return o;
	}

	/**
	 * Method will return 'true' if the error message exists for any components
	 * not supplied.
	 * 
	 * @param components
	 *            - components
	 * @return
	 */
	public static boolean doesPageComponentsHaveError(UIComponent... components) {
		// if no components, then no cross field validator
		if (components == null || components.length == 0) {
			return false;
		}

		// get all hidden components id as list
		List<String> componentIdList = new ArrayList<String>();
		for (UIComponent component : components) {
			componentIdList.add(component.getId());
		}

		Iterator<String> messageIter = JsfUtils.getFacesContext().getClientIdsWithMessages();
		while (messageIter.hasNext()) {
			String clientId = messageIter.next();
			for (String componentId : componentIdList) {
				if (componentId.equals(clientId)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Refreshes current JSF page, which make postback 'false'.
	 */
	public static void refreshCurrentPage() {
		FacesContext context = getFacesContext();
		String currentView = context.getViewRoot().getViewId();

		ViewHandler vh = context.getApplication().getViewHandler();
		UIViewRoot x = vh.createView(context, currentView);
		x.setViewId(currentView);
		context.setViewRoot(x);
	}

	public static UIComponent findComponent(String pComponentId) {
		UIComponent component = null;

		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null) {
			UIComponent root = facesContext.getViewRoot();
			component = findComponent(root, pComponentId);
		}

		return component;
	}

	/**
	 * Returns context path of the web project.
	 * 
	 * @return
	 */
	public static String getContextPath() {
		ServletContext servletContext = (ServletContext) getExternalContext().getContext();
		return servletContext.getContextPath();
	}

	public static String getSessionId() {
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		return session.getId();
	}

	public static final String NO_RESOURCE_FOUND = "???";

	/**
	 * Method for taking a reference to a JSF binding expression and returning
	 * the matching object (or creating it).
	 * 
	 * @param expression
	 *            EL expression
	 * @return Managed object
	 */
	public static Object resolveExpression(String expression) {
		FacesContext facesContext = getFacesContext();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		ValueExpression valueExp = elFactory.createValueExpression(elContext, expression, Object.class);
		return valueExp.getValue(elContext);
	}

	/**
	 * @return
	 */
	public static String resolveRemoteUser() {
		FacesContext facesContext = getFacesContext();
		ExternalContext ectx = facesContext.getExternalContext();
		return ectx.getRemoteUser();
	}

	/**
	 * @return
	 */
	public static String resolveUserPrincipal() {
		FacesContext facesContext = getFacesContext();
		ExternalContext ectx = facesContext.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) ectx.getRequest();
		return request.getUserPrincipal().getName();
	}

	/**
	 * @param expression
	 * @param returnType
	 * @param argTypes
	 * @param argValues
	 * @return
	 */
	public static Object resolveMethodExpression(String expression, Class<?> returnType, Class[] argTypes,
			Object[] argValues) {
		FacesContext facesContext = getFacesContext();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		MethodExpression methodExpression = elFactory.createMethodExpression(elContext, expression, returnType,
				argTypes);
		return methodExpression.invoke(elContext, argValues);
	}

	/**
	 * Method for taking a reference to a JSF binding expression and returning
	 * the matching Boolean.
	 * 
	 * @param expression
	 *            EL expression
	 * @return Managed object
	 */
	public static Boolean resolveExpressionAsBoolean(String expression) {
		return (Boolean) resolveExpression(expression);
	}

	/**
	 * Method for taking a reference to a JSF binding expression and returning
	 * the matching String (or creating it).
	 * 
	 * @param expression
	 *            EL expression
	 * @return Managed object
	 */
	public static String resolveExpressionAsString(String expression) {
		return (String) resolveExpression(expression);
	}

	/**
	 * Convenience method for resolving a reference to a managed bean by name
	 * rather than by expression.
	 * 
	 * @param beanName
	 *            name of managed bean
	 * @return Managed object
	 */
	public static Object getManagedBeanValue(String beanName) {
		StringBuffer buff = new StringBuffer("#{");
		buff.append(beanName);
		buff.append("}");
		return resolveExpression(buff.toString());
	}

	/**
	 * Method for setting a new object into a JSF managed bean Note: will fail
	 * silently if the supplied object does not match the type of the managed
	 * bean.
	 * 
	 * @param expression
	 *            EL expression
	 * @param newValue
	 *            new value to set
	 */
	public static void setExpressionValue(String expression, Object newValue) {
		FacesContext facesContext = getFacesContext();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		ValueExpression valueExp = elFactory.createValueExpression(elContext, expression, Object.class);

		// Check that the input newValue can be cast to the property type
		// expected by the managed bean.
		// If the managed Bean expects a primitive we rely on Auto-Unboxing
		Class<?> bindClass = valueExp.getType(elContext);
		if (bindClass.isPrimitive() || bindClass.isInstance(newValue)) {
			valueExp.setValue(elContext, newValue);
		}
	}

	/**
	 * Convenience method for setting the value of a managed bean by name rather
	 * than by expression.
	 * 
	 * @param beanName
	 *            name of managed bean
	 * @param newValue
	 *            new value to set
	 */
	public static void setManagedBeanValue(String beanName, Object newValue) {
		StringBuffer buff = new StringBuffer("#{");
		buff.append(beanName);
		buff.append("}");
		setExpressionValue(buff.toString(), newValue);
	}

	/**
	 * Convenience method for setting Session variables.
	 * 
	 * @param key
	 *            object key
	 * @param object
	 *            value to store
	 */

	public static void storeOnSession(String key, Object object) {
		FacesContext ctx = getFacesContext();
		Map<String, Object> sessionState = ctx.getExternalContext().getSessionMap();
		sessionState.put(key, object);
	}

	/**
	 * Convenience method for getting Session variables.
	 * 
	 * @param key
	 *            object key
	 * @return session object for key
	 */
	public static Object getFromSession(String key) {
		FacesContext ctx = getFacesContext();
		Map<String, Object> sessionState = ctx.getExternalContext().getSessionMap();
		return sessionState.get(key);
	}

	/**
	 * @param key
	 * @return
	 */
	public static String getFromHeader(String key) {
		FacesContext ctx = getFacesContext();
		ExternalContext ectx = ctx.getExternalContext();
		return ectx.getRequestHeaderMap().get(key);
	}

	/**
	 * Convenience method for getting Request variables.
	 * 
	 * @param key
	 *            object key
	 * @return session object for key
	 */
	public static Object getFromRequest(String key) {
		FacesContext ctx = getFacesContext();
		Map<String, Object> sessionState = ctx.getExternalContext().getRequestMap();
		return sessionState.get(key);
	}

	/**
	 * Pulls a String resource from the property bundle that is defined under
	 * the application &lt;message-bundle&gt; element in the faces config.
	 * Respects Locale
	 * 
	 * @param key
	 *            string message key
	 * @return Resource value or placeholder error String
	 */
	public static String getStringFromBundle(String key) {
		ResourceBundle bundle = getApplicationBundle();
		return getStringSafely(bundle, key);
	}

	/**
	 * Convenience method to construct a <code>FacesMesssage</code> from a
	 * defined error key and severity This assumes that the error keys follow
	 * the convention of using <b>_detail</b> for the detailed part of the
	 * message, otherwise the main message is returned for the detail as well.
	 * 
	 * @param key
	 *            for the error message in the resource bundle
	 * @param severity
	 *            severity of message
	 * @return Faces Message object
	 */
	public static FacesMessage getMessageFromAppBundle(String key, FacesMessage.Severity severity) {
		ResourceBundle bundle = getApplicationBundle();
		String summary = getStringSafely(bundle, key);
		String detail = getStringSafely(bundle, key + "_detail");
		FacesMessage message = new FacesMessage(summary, detail);
		message.setSeverity(severity);
		return message;
	}

	/**
	 * Add JSF info message.
	 * 
	 * @param msg
	 *            info message string
	 */
	public static void addFacesInformationMessage(String msg) {
		FacesContext ctx = getFacesContext();
		FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, "");
		ctx.addMessage(getRootViewComponentId(), fm);
	}

	public static void addFacesInformationMessage(String msg, String componentId) {
		FacesContext ctx = getFacesContext();
		FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, "");
		ctx.addMessage(componentId, fm);
	}

	/**
	 * Add JSF error message.
	 * 
	 * @param msg
	 *            error message string
	 */
	public static void addFacesErrorMessage(String msg) {
		FacesContext ctx = getFacesContext();
		FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, "");
		ctx.addMessage(getRootViewComponentId(), fm);
	}

	/**
	 * Add JSF error message for a specific attribute.
	 * 
	 * @param attrName
	 *            name of attribute
	 * @param msg
	 *            error message string
	 */
	public static void addFacesErrorMessage(String attrName, String msg) {
		FacesContext ctx = getFacesContext();
		FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_ERROR, attrName, msg);
		ctx.addMessage(getRootViewComponentId(), fm);
	}

	/**
	 * Returns message from a bundle.
	 * 
	 * @param argBundleName
	 * @param argMessageId
	 * @param argParameterValues
	 * @return
	 */
	public static String getMessageFromBundle(String argBundleName, String key, Object... argParameterValues) {
		ResourceBundle bundle = getBundle(argBundleName);
		return getMessageFromBundle(bundle, key, argParameterValues);
	}

	/**
	 * Returns message from a bundle.
	 * 
	 * @param argBundleName
	 * @param argMessageId
	 * @param argParameterValues
	 * @return
	 */
	public static String getMessageFromBundle(ResourceBundle bundle, String key, Object... argParameterValues) {
		if (bundle == null) {
			return key;
		}
		try {
			String value = bundle.getString(key);
			return new MessageFormat(value).format(argParameterValues);
		} catch (MissingResourceException mrex) {
			return NO_RESOURCE_FOUND + key;
		}

	}

	/**
	 * Create FacesMessage from Bundle.
	 * 
	 * @param argBundleName
	 * @param argMessageId
	 * @param argParameterValues
	 * @return
	 */
	public static FacesMessage getFacesMessage(String argBundleName, String argMessageId,
			Object... argParameterValues) {
		String message = getMessageFromBundle(argBundleName, argMessageId, argParameterValues);
		return new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message);
	}

	// Informational getters

	/**
	 * Get view id of the view root.
	 * 
	 * @return view id of the view root
	 */
	public static String getRootViewId() {
		return getFacesContext().getViewRoot().getViewId();
	}

	/**
	 * Get component id of the view root.
	 * 
	 * @return component id of the view root
	 */
	public static String getRootViewComponentId() {
		return getFacesContext().getViewRoot().getId();
	}

	/**
	 * Get FacesContext.
	 * 
	 * @return FacesContext
	 */
	public static FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	/*
	 * Internal method to pull out the correct local message bundle
	 */

	private static ResourceBundle getApplicationBundle() {
		FacesContext ctx = getFacesContext();
		UIViewRoot uiRoot = ctx.getViewRoot();
		Locale locale = uiRoot.getLocale();
		ClassLoader ldr = Thread.currentThread().getContextClassLoader();
		return ResourceBundle.getBundle(ctx.getApplication().getMessageBundle(), locale, ldr);
	}

	/**
	 * Get an HTTP Request attribute.
	 * 
	 * @param name
	 *            attribute name
	 * @return attribute value
	 */
	public static Object getRequestAttribute(String name) {
		return getFacesContext().getExternalContext().getRequestMap().get(name);
	}

	/**
	 * Set an HTTP Request attribute.
	 * 
	 * @param name
	 *            attribute name
	 * @param value
	 *            attribute value
	 */
	public static void setRequestAttribute(String name, Object value) {
		getFacesContext().getExternalContext().getRequestMap().put(name, value);
	}

	public static String getStringSafely(ResourceBundle resourceBundle, String key) {
		String resource = null;
		if (resourceBundle == null) {
			return key;
		}
		try {
			resource = resourceBundle.getString(key);
		} catch (MissingResourceException mrex) {
			resource = NO_RESOURCE_FOUND + key;

		}
		return resource;
	}

	/*
	 * Internal method to proxy for resource keys that don't exist
	 */

	public static String getStringSafely(String bundleName, String key) {
		ResourceBundle resourceBundle = getBundle(bundleName);
		return getStringSafely(resourceBundle, key);
	}

	/**
	 * Locate an UIComponent in view root with its component id. Use a recursive
	 * way to achieve this.
	 * 
	 * @param id
	 *            UIComponent id
	 * @return UIComponent object
	 */
	public static UIComponent findComponentInRoot(String id) {
		UIComponent component = null;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null) {
			UIComponent root = facesContext.getViewRoot();
			component = findComponent(root, id);
		}
		return component;
	}

	/**
	 * Locate an UIComponent from its root component. Taken from
	 * http://www.jroller.com/page/mert?entry=how_to_find_a_uicomponent
	 * 
	 * @param base
	 *            root Component (parent)
	 * @param id
	 *            UIComponent id
	 * @return UIComponent object
	 */
	public static UIComponent findComponent(UIComponent base, String id) {
		if (id.equals(base.getId()))
			return base;

		UIComponent children = null;
		UIComponent result = null;
		Iterator<?> childrens = base.getFacetsAndChildren();
		while (childrens.hasNext() && (result == null)) {
			children = (UIComponent) childrens.next();
			System.out.println("JsfUtils.findComponent() id=" + children.getId());
			if (id.equals(children.getId())) {
				result = children;
				break;
			}
			result = findComponent(children, id);
			if (result != null) {
				break;
			}
		}
		return result;
	}

	/**
	 * Method to create a redirect URL. The assumption is that the JSF servlet
	 * mapping is "faces", which is the default
	 *
	 * @param view
	 *            the JSP or JSPX page to redirect to
	 * @return a URL to redirect to
	 */
	public static String getPageURL(String view) {
		FacesContext facesContext = getFacesContext();
		ExternalContext externalContext = facesContext.getExternalContext();
		String url = ((HttpServletRequest) externalContext.getRequest()).getRequestURL().toString();
		StringBuffer newUrlBuffer = new StringBuffer();
		newUrlBuffer.append(url.substring(0, url.lastIndexOf("faces/")));
		newUrlBuffer.append("faces");
		String targetPageUrl = view.startsWith("/") ? view : "/" + view;
		newUrlBuffer.append(targetPageUrl);
		return newUrlBuffer.toString();
	}

	public static ValueExpression createValueExpression(String valueValueExpression, Class<?> valueType) {
		FacesContext facesContext = getFacesContext();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		return elFactory.createValueExpression(elContext, valueValueExpression, valueType);
	}

	public static MethodExpression createMethodExpression(String methodExpression, Class<?> class1, Class<?>[] clazz) {
		FacesContext facesContext = getFacesContext();
		Application app = facesContext.getApplication();
		ExpressionFactory elFactory = app.getExpressionFactory();
		ELContext elContext = facesContext.getELContext();
		return elFactory.createMethodExpression(elContext, methodExpression, class1, clazz);
	}

	public static int getMaximumSeverity() {
		if (getFacesContext().getMaximumSeverity() == null) {
			return 0;
		}
		return getFacesContext().getMaximumSeverity().getOrdinal();
	}

	public static boolean isMessages() {
		return (getFacesContext().getMessageList() != null) && (getFacesContext().getMessageList().isEmpty() == false);
	}

	/**
	 * @param contentType
	 * @param fileName
	 * @param data
	 * @throws IOException 
	 */
	public static void downloadFile(String contentType, String fileName, byte[] data) throws IOException {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		// Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to
		// get rid of them, else it may collide.
		ec.responseReset();
		// Check http://www.iana.org/assignments/media-types for all types. Use if necessary
		// ExternalContext#getMimeType() for
		// auto-detection based on filename.
		ec.setResponseContentType(contentType);
		// ec.setResponseContentLength(contentLength); // Set it with the file size. This header is optional. It will
		// work if it's omitted,
		// but the download progress will be unknown.
		// The Save As popup magic is done here. You can give it any file
		// name you want, this only won't work in MSIE, it will use current request URL as file name instead.
		ec.setResponseHeader("Content-Disposition", "attachment; filename=" + fileName);
		ec.setResponseCharacterEncoding("utf-8");
		OutputStream output;
		output = ec.getResponseOutputStream();
		output.write(data);
		output.flush();
		output.close();
		fc.responseComplete(); // Important! Otherwise JSF will attempt to render the response which obviously will fail
								// since it's already
								// written with a file and closed.
	}

//	public static <T> T findJsfBean(Class<T> klass) {
//		String name = klass.getSimpleName().substring(0, 1).toLowerCase()
//				+ klass.getSimpleName().substring(1, klass.getSimpleName().length());
//		return findJsfBean(name);
//	}
//
//	@SuppressWarnings("unchecked")
//	public static <T> T findJsfBean(String beanName) {
//		FacesContext context = FacesContext.getCurrentInstance();
//		if (context == null) {
//			return null;
//		}
//		return (T) context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
//	}
//
//	public static <T> T findJsfBean(HttpServletRequest httpServletRequest, Class<T> klass) {
//		String name = klass.getSimpleName().substring(0, 1).toLowerCase()
//				+ klass.getSimpleName().substring(1, klass.getSimpleName().length());
//		return findJsfBean(httpServletRequest, name);
//	}

//	@SuppressWarnings("unchecked")
//	public static <T> T findJsfBean(HttpServletRequest httpServletRequest, String beanName) {
//		return (T) httpServletRequest.getSession().getAttribute(beanName);
//	}

}
