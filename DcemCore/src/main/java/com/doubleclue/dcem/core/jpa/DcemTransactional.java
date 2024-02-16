package com.doubleclue.dcem.core.jpa;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.interceptor.InterceptorBinding;

@Retention(RUNTIME)
@Target({METHOD, TYPE})
@Documented
@InterceptorBinding
public @interface DcemTransactional {}
