package com.doubleclue.dcem.core.gui.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * This validator check for a proper String. The String is proper if:
 * - The String is not null
 * - The (trimmed) String is not empty
 * 
 * @author Emanuel Galea
 *
 */
public class NotNullOrEmptyStringValidator implements ConstraintValidator<NotNullOrEmptyString, String> {

	@Override
	public void initialize(NotNullOrEmptyString stringValue) {
	}

	@Override
	public boolean isValid(String stringValue, ConstraintValidatorContext ctx) {

		if (stringValue == null || stringValue.trim().isEmpty()) {
			return false;
		}

		return true;

	}

}
