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
@Constraint(validatedBy = IpNumberValidatorImpl.class)
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IpNumberValidator {

    String message() default "Wrong IP Number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}