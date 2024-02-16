package com.doubleclue.dcem.core.utils;

import java.lang.reflect.Method;

import com.doubleclue.dcem.core.weld.CdiUtils;

public class ReflectionUtils {

	public static void runMethod(String className, String methodName, Object... args) throws Exception {
		Class<?> klass = Class.forName(className);
		Object object = CdiUtils.getReference(klass);
		Method method;
		int argCount = args.length;
		if (argCount > 0) {
			Class<?>[] argClasses = new Class<?>[argCount];
			for (int i = 0; i < argCount; i++) {
				argClasses[i] = args[i].getClass();
			}
			method = klass.getMethod(methodName, argClasses);
		} else {
			method = klass.getMethod(methodName);
		}
		method.invoke(object, args);
	}
}
