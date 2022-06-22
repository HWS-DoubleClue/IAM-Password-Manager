package com.doubleclue.dcem.core.weld;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

/**
 * this class contains some functions used for containers, that do not support
 * CDI completely. F.e. Tomcat6 does not support injection into ServletListeners
 * or JAX-WS webservices, because Tomcat does give CDI (Weld) a chance to
 * delegate object creation to CDI.
 * 
 * @author Emanuel Galea
 * 
 */
public class CdiUtils {

	private CdiUtils() {
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
			throw new RuntimeException("could not find implementation for " + clazz.getName() + " annotated with " + Arrays.asList(annotations));
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

}
