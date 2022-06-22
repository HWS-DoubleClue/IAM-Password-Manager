package com.doubleclue.portaldemo.utils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Note: Internal Use only.
 *
 * Base Message Handler class, which will read property file and replaces
 * parameters with place holders {0},{1},etc.
 *
 */
public class MessageBundle {
	// Fmessage bundle location. Locale is appended at runtime
	private String sMessageBundleName = null;
	private static final String NO_RESOURCE_FOUND = "** No Resource found for ";

	// Holds a bundle for each locale
	private Map<Locale, PropertyResourceBundle> mBundles = new HashMap<Locale, PropertyResourceBundle>();

	/**
	 * Set the message bundle file name
	 * 
	 * @param messageBundleName
	 */
	public MessageBundle(String messageBundleName) {
		sMessageBundleName = messageBundleName;
	}

	/**
	 * Returns FacesMessage object for a given message id and parameterValues
	 * object array.
	 *
	 * @param messageId
	 * @param parameterValues
	 * @return
	 */
	public String getMessage(String messageId, Object... parameterValues) {
		// if no message id then return null
		if (messageId == null) {
			return null;
		}

		// get the locale from UIViewRoot of FacesContext
		Locale locale = JsfUtils.getLocale();

		// get the message
		String message = getMessage(messageId, locale);

		if (message != null && parameterValues != null) {
			message = replaceWithParameters(message, parameterValues);
		}

		return message;
	}

	/**
	 * Return message for the messageId and locale.
	 * 
	 * @param messageId
	 * @param locale
	 * @return
	 */
	private String getMessage(String messageId, Locale locale) {
		// check whether bundle is already loaded
		// if not load and store in bundle map.
		loadBundle(locale);

		return getStringSafely((mBundles.get(locale)), messageId, null);
	}

	/**
	 * Checks to see whether key exists
	 * 
	 * @param messageId
	 * @return
	 */
	public boolean containsMessage(String messageId) {
		Locale locale = JsfUtils.getLocale();

		// check whether bundle is already loaded
		// if not load and store in bundle map.
		loadBundle(locale);

		return mBundles.get(locale).containsKey(messageId);
	}

	private void loadBundle(Locale locale) {
		if (mBundles.get(locale) == null) {
			synchronized (locale) {
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle
						.getBundle(sMessageBundleName, locale, classLoader);
				mBundles.put(locale, bundle);
			}
		}
	}

	/**
	 * Replaces {0}, {1}, etc with respective parameter values supplied.
	 * 
	 * @param message
	 * @param parameterValues
	 * @return
	 */
	private String replaceWithParameters(String message, Object... parameterValues) {
		return new MessageFormat(message).format(parameterValues);
	}

	/*
	 * Internal method to proxy for resource keys that don't exist
	 */

	private static String getStringSafely(ResourceBundle bundle, String key, String defaultValue) {
		String resource = null;
		try {
			resource = bundle.getString(key);
		} catch (Exception exp) {
		}
		if (resource == null) {
			if (defaultValue != null) {
				resource = defaultValue;
			} else {
				resource = NO_RESOURCE_FOUND + key;
			}
		}
		return resource;

	}

	/*
	 * Internal method to pull out the correct local message bundle
	 */
//
//	private static ResourceBundle getBundle() {
//		FacesContext ctx = FacesContext.getCurrentInstance();
//		UIViewRoot uiRoot = ctx.getViewRoot();
//		Locale locale = uiRoot.getLocale();
//		ClassLoader ldr = Thread.currentThread().getContextClassLoader();
//		return ResourceBundle.getBundle(ctx.getApplication().getMessageBundle(), locale, ldr);
//	}
}
