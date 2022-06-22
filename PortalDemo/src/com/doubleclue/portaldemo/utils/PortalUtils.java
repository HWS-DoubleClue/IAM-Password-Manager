package com.doubleclue.portaldemo.utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Named;

/**
 * this class contains some functions used for containers, that do not support
 * CDI completely. F.e. Tomcat6 does not support injection into ServletListeners
 * or JAX-WS webservices, because Tomcat does give CDI (Weld) a chance to
 * delegate object creation to CDI.
 * 
 * @author Emanuel Galea
 * 
 */
@Named("portalUtils")
@ApplicationScoped
public class PortalUtils {

	private PortalUtils() {
	}
	
	public String getCookieRoute() {
		return System.getProperty("jvmRoute");
	}

	public String getVersion() {
		try {
			Attributes attributes = getManifestInformation(PortalUtils.class);
			if (attributes == null) {
				return "99.99.99-Unknown";
			} else {
				return attributes.getValue(Name.IMPLEMENTATION_VERSION);
			}
		} catch (Exception e) {
			return "1.0.0";
		}
	}

	static BeanManager beanManager = null;

	public static BeanManager getBeanManager() {
		// get the BeanManager from JNDI (tomcat: java:comp/env/BeanManager)
		if (beanManager == null) {
			beanManager = CDI.current().getBeanManager();
		}
		return beanManager;
	}

	/**
	 * get CDI managed Bean reference with given qualifier annotations
	 * 
	 * @param clazz
	 *            Class of the bean to get a reference of
	 * @param annotations
	 *            qualifiers (if omitted, @Default will be used)
	 */
	public static <T> T getReference(Class<T> clazz, Annotation... annotations) {
		BeanManager beanManager = getBeanManager();

		Set<Bean<?>> beans = beanManager.getBeans(clazz, annotations);
		if (beans.size() == 0) {
			throw new RuntimeException("could not find implementation for " + clazz.getName() + " annotated with "
					+ Arrays.asList(annotations));
		}
		@SuppressWarnings("unchecked")
		Bean<T> bean = (Bean<T>) beans.iterator().next();
		CreationalContext<T> context = beanManager.createCreationalContext(bean);
		@SuppressWarnings("unchecked")
		T beanReference = (T) beanManager.getReference(bean, clazz, context);
		return beanReference;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> getReferences(final Class<T> type, Annotation... annotations) {
		BeanManager beanManager = getBeanManager();

		List<T> result = new ArrayList<T>();
		for (Bean<?> bean : beanManager.getBeans(type, annotations)) {
			CreationalContext<T> context = (CreationalContext<T>) beanManager.createCreationalContext(bean);
			if (context != null) {
				result.add((T) beanManager.getReference(bean, type, context));
			}
		}
		return result;
	}

	/**
	 * get CDI managed Bean reference with given qualifier annotations
	 * 
	 * @param beanName
	 *            Class of the bean to get a reference of
	 * @param annotations
	 *            qualifiers (if omitted, @Default will be used)
	 */
	public static <T> T getReference(String beanName) {
		BeanManager beanManager = getBeanManager();

		Set<Bean<?>> beans = beanManager.getBeans(beanName);
		if (beans.size() == 0) {
			throw new RuntimeException("could not find implementation for " + beanName);
		}
		@SuppressWarnings("unchecked")
		Bean<T> bean = (Bean<T>) beans.iterator().next();
		CreationalContext<T> context = beanManager.createCreationalContext(bean);
		@SuppressWarnings("unchecked")
		T beanReference = (T) beanManager.getReference(bean, bean.getBeanClass(), context);
		return beanReference;
	}

	static public Attributes getManifestInformation(Class<?> clazz) throws IOException {

		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		if (classPath.startsWith("jar") == true) {
			String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
			Manifest manifest;
			manifest = new Manifest(new URL(manifestPath).openStream());
			return manifest.getMainAttributes();
		} else {
			// ONLX in Development environment
			int ind = classPath.indexOf("/bin/");
			URL url;
			if (ind > 0) {
				String urlPath = classPath.substring(0, ind + 5) + "META-INF/MANIFEST.MF";
				url = new URL(urlPath);
			} else {
				ind = classPath.indexOf("/target/classes/");
				if (ind > 0) {
					String urlPath = classPath.substring(0, ind + "/target/classes/".length()) + "META-INF/MANIFEST.MF";
					url = new URL(urlPath);
				} else {
					return null;
				}
			}
			Manifest manifest = new Manifest(url.openStream());
			return manifest.getMainAttributes();
		}
	}

}
