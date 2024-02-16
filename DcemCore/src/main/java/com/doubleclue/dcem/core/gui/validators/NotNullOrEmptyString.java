package com.doubleclue.dcem.core.gui.validators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 *
 */
@Constraint(validatedBy = NotNullOrEmptyStringValidator.class)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NotNullOrEmptyString {

	String message() default "String must not be null or empty.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
