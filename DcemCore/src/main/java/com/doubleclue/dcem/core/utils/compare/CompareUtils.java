package com.doubleclue.dcem.core.utils.compare;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Persistence;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.utils.DateUtils;
import com.doubleclue.dcem.core.utils.DcemUtils;

public class CompareUtils {

	private static final Logger logger = LogManager.getLogger(CompareUtils.class);
	final static String FIN = ";  ";
	final static String ARROW_RIGHT = " >  ";
	final static String MODIFIED = "-MODIFIED-";

	public static String compareObjects(final Object oldObject, final Object newObject) throws CompareException {
		if (oldObject == null && newObject == null) {
			return "";
		}
		StringBuffer stringBuffer = new StringBuffer();
		Object comapreObject = compare(oldObject, newObject, stringBuffer);
		if (stringBuffer.isEmpty() == false) {
			return "[" + comapreObject.toString() + "] = " + stringBuffer.toString();
		}
		return stringBuffer.toString();
	}

	/**
	 * @param oldObject   Object
	 * @param newObject   Object
	 * @param isNewObject boolean
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws DcemException
	 */
	private static Object compare(final Object oldObject, final Object newObject, StringBuffer stringBuilder) throws CompareException {
		Object compareObject = null;
		try {
			if (oldObject == null && newObject == null) {
				return null;
			}
			boolean oneObject = false;
			if (oldObject == null) {
				oneObject = true;
				compareObject = newObject;
			} else if (newObject == null) {
				oneObject = true;
				compareObject = oldObject;
			} else {
				compareObject = newObject;
			}
			Class<?> objectClass = compareObject.getClass();
			boolean isEntity = objectClass.getSuperclass().equals(EntityInterface.class);
			Field newField;
			int modifiers;
			DcemCompare compareAnnotation;
			for (Field field : objectClass.getDeclaredFields()) {
				modifiers = field.getModifiers();
				if (Modifier.isStatic(modifiers) || (Modifier.isFinal(modifiers)) || (Modifier.isTransient(modifiers))) {
					continue;
				}
				compareAnnotation = field.getAnnotation(DcemCompare.class);
				if (compareAnnotation != null && compareAnnotation.ignore() == true) {
					continue;
				}
				if (field.getName().startsWith("$")) {
					continue;
				}
				newField = objectClass.getDeclaredField(field.getName());
				newField.setAccessible(true);
				field.setAccessible(true);
				if (isEntity == true && Persistence.getPersistenceUtil().isLoaded(compareObject, field.getName()) == false) {
					continue;
				}
				if (field.getAnnotation(javax.persistence.Version.class) != null) {
					continue;
				}
				Method getterMethod = DcemUtils.getGetterMethodFromString(field.getName(), objectClass);
				Class<?> cls = field.getType();
				if (cls.equals(String.class)) {
					if (oneObject == true) {
						stringBuilder.append(field.getName());
						stringBuilder.append(": ");
						if (compareAnnotation != null && compareAnnotation.password()) {
							stringBuilder.append("PASSWORD");
						} else {
							if (compareAnnotation != null && compareAnnotation.withoutResult()) {
								stringBuilder.append(MODIFIED);
							} else {
								stringBuilder.append(getterMethod.invoke(compareObject));
							}
						}
					} else {
						String oldString = (String) getterMethod.invoke(oldObject);
						String newString = (String) getterMethod.invoke(newObject);
						if (Objects.equals(oldString, newString) == false) {
							stringBuilder.append(field.getName());
							stringBuilder.append(": ");
							if (compareAnnotation != null && compareAnnotation.password()) {
								stringBuilder.append(newString.equals(null) ? "" : "-PASSWORD-");
							} else {
								if (compareAnnotation != null && compareAnnotation.withoutResult()) {
									stringBuilder.append(MODIFIED);
								} else {
									stringBuilder.append(oldString);
									stringBuilder.append(ARROW_RIGHT);
									stringBuilder.append(newString);
								}
							}
							stringBuilder.append(FIN);
						}
					}
				} else if (cls.equals(Boolean.class) || cls.isEnum() == true || cls.isPrimitive() || cls.equals(Long.class) == true
						|| cls.equals(Integer.class)) {
					compareField(stringBuilder, field, getterMethod, oldObject, newObject);
				} else if (cls.equals(Map.class)) {
					Map<Object, Object> oldMap = (Map) getterMethod.invoke(oldObject);
					Map<Object, Object> newMap = (Map) getterMethod.invoke(newObject);
					for (Object object : oldMap.keySet()) {
						if (oldMap.get(object).equals(newMap.get(object)) == false) {
							stringBuilder.append(object.toString());
							stringBuilder.append(": ");
							stringBuilder.append(oldMap.get(object).toString());
							stringBuilder.append(ARROW_RIGHT);
							stringBuilder.append(newMap.get(object).toString());
							stringBuilder.append(FIN);
						}
					}
				} else if (cls.equals(byte[].class)) {
					continue;
				} else {
					if (compareAnnotation == null || compareAnnotation.deepCompare() == false) {
						compareField(stringBuilder, field, getterMethod, oldObject, newObject);
					} else {
						Object oldFieldObject = getterMethod.invoke(oldObject);
						Object newFieldObject = getterMethod.invoke(newObject);
						compare(oldFieldObject, newFieldObject, stringBuilder);
					}
				}
			}
			return compareObject;
		} catch (Exception exp) {
			logger.info("Compare-Objects", exp);
			throw new CompareException(compareObject.getClass().getName(), exp);
		}
	}

	static private void compareField(StringBuffer stringBuilder, Field field, Method getterMethod, Object oldObject, Object newObject) throws Exception {
		Object compareObject = null;
		if (oldObject == null) {
			compareObject = newObject;
		} else if (newObject == null) {
			compareObject = oldObject;
		}
		if (compareObject != null) {
			stringBuilder.append(field.getName());
			stringBuilder.append(": ");
			stringBuilder.append(getterMethod.invoke(compareObject));
			stringBuilder.append(FIN);
		} else {
			Object newValue = getterMethod.invoke(newObject);
			Object oldValue = getterMethod.invoke(oldObject);
			if (newValue instanceof Collection) {
				compareColletions(field.getName(), (Collection) oldValue, (Collection) newValue, stringBuilder);
			} else {
				if (Objects.deepEquals(newValue, oldValue) == false) {
					stringBuilder.append(field.getName());
					stringBuilder.append(": ");
					stringBuilder.append(oldValue);
					stringBuilder.append(ARROW_RIGHT);
					stringBuilder.append(newValue);
					stringBuilder.append(FIN);
				}
			}
		}
	}

	static void compareColletions(String name, Collection<Object> oldCollection, Collection<?> newCollection, StringBuffer sb) throws Exception {
		HashSet<Object> foundOld = new HashSet<>();
		Object oldEntity, newEntity;
		Iterator<Object> iterator = oldCollection.iterator();
		StringBuffer collectionSb = new StringBuffer();
		while (iterator.hasNext()) {
			oldEntity = iterator.next();
			if (oldEntity instanceof EntityInterface) {
				newEntity = entityContains((Collection<EntityInterface>) newCollection, (EntityInterface) oldEntity);
				if (newEntity == null) {
					collectionSb.append("-(" + oldEntity + ") ");
				} else {
					foundOld.add(newEntity);
					compare(oldEntity, newEntity, collectionSb);
				}
			} else {
				// standard Objet
				if (newCollection.contains(oldEntity) == false) {
					collectionSb.append("-(" + oldEntity+ ") ");
				}
			}
	//		System.out.println("CompareUtils.compareColletions() " + sb.toString());
		}
		Iterator<?> newIterator = newCollection.iterator();
		while (newIterator.hasNext()) {
			newEntity = newIterator.next();
			if (foundOld.contains(newEntity)) {
				continue;
			}
			collectionSb.append("+(" + newEntity + ") " );
		}
		if (collectionSb.isEmpty() == false) {
			sb.append("[");
			sb.append(name);
			sb.append(": ");
			sb.append(collectionSb);
			sb.append("] ");
		}
	}

	static EntityInterface entityContains(Collection<EntityInterface> collection, EntityInterface entity) {
		Iterator<EntityInterface> iterator = collection.iterator();
		while (iterator.hasNext()) {
			EntityInterface entityA = iterator.next();
			if (entityA.getId().equals(entity.getId())) {
				return entityA;
			}
		}
		return null;
	}

	static public void copyObject(Object sourceObject, Object destObject) {
		Method[] gettersAndSetters = sourceObject.getClass().getMethods();
		for (int i = 0; i < gettersAndSetters.length; i++) {
			String methodName = gettersAndSetters[i].getName();
			try {
				if (methodName.startsWith("get")) {
					destObject.getClass().getMethod(methodName.replaceFirst("get", "set"), gettersAndSetters[i].getReturnType()).invoke(destObject,
							gettersAndSetters[i].invoke(sourceObject));
				} else if (methodName.startsWith("is")) {
					destObject.getClass().getMethod(methodName.replaceFirst("is", "set"), gettersAndSetters[i].getReturnType()).invoke(destObject,
							gettersAndSetters[i].invoke(sourceObject));
				}
			} catch (NoSuchMethodException exp) {
			} catch (Exception exp) {
				logger.debug(exp);
			}

		}
		return;
	}
}
