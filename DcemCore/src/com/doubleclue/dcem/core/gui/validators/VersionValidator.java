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
@Constraint(validatedBy = VersionValidatorImpl.class)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface VersionValidator {

	String message() default "Version must have the format MM.mm.rv";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
