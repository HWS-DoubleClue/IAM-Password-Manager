package com.doubleclue.dcem.core.gui.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * This validator check for a proper String. The String is proper if: - The
 * String is not null - The (trimmed) String is not empty
 * 
 * @author Emanuel Galea
 *
 */
public class IpNumberValidatorImpl implements ConstraintValidator<IpNumberValidator, String> {

	private static Pattern VALID_IPV4_PATTERN = null;
	private static Pattern VALID_IPV6_PATTERN = null;
	private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	private static final String ipv6Pattern = "([0-9a-f]{1,4}:){7}([0-9a-f]){1,4}";

	static {
		try {
			VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
			VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException e) {
			// logger.severe("Unable to compile pattern", e);
		}
	}

	@Override
	public void initialize(IpNumberValidator stringValue) {
	}

	@Override
	public boolean isValid(String ipAddress, ConstraintValidatorContext ctx) {
		if (ipAddress == null || ipAddress.isEmpty()) {
			return false;
		}
		Matcher m1 = VALID_IPV4_PATTERN.matcher(ipAddress);
		if (m1.matches()) {
			return true;
		}
		Matcher m2 = VALID_IPV6_PATTERN.matcher(ipAddress);
		return m2.matches();

	}
}
