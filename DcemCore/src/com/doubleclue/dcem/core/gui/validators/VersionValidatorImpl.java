package com.doubleclue.dcem.core.gui.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.doubleclue.utils.ProductVersion;

/**
 * This validator check for a proper String. The String is proper if:
 * - The String is not null
 * - The (trimmed) String is not empty
 * 
 * @author Emanuel Galea
 *
 */
public class VersionValidatorImpl implements ConstraintValidator<VersionValidator, String> {

	@Override
	public void initialize(VersionValidator stringValue) {
	}

	@Override
	public boolean isValid(String versionStr, ConstraintValidatorContext ctx) {

		try {
			new ProductVersion(null, versionStr);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
