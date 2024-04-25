package com.doubleclue.dcem.core.utils.compare;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * @author Emanuel Galea
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DcemCompare
{
	public boolean password() default false;  		// Field is a password field
	public boolean ignore() default false;
	public boolean withoutResult() default false;
	public boolean deepCompare() default false;
}
