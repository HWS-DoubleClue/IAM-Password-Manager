package com.doubleclue.dcem.core.jersey;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

/**
 * Custom Security Context.
 * 
 * @author Emanuel Galea not in use.
*/
@Deprecated
public class RestSecuritycontext implements SecurityContext {
	private PrincipalOperator principalOperator;
	private String scheme;

	public RestSecuritycontext(PrincipalOperator principalOperator, String scheme) {
		this.principalOperator = principalOperator;
		this.scheme = scheme;
	}

	@Override
	public Principal getUserPrincipal() {
		return this.principalOperator;
	}

	@Override
	public boolean isUserInRole(String s) {
		// if (user.getRole() != null) {
		// return user.getRole().contains(s);
		// }
		return false;
	}

	@Override
	public boolean isSecure() {
		return "https".equals(this.scheme);
	}

	@Override
	public String getAuthenticationScheme() {
		return SecurityContext.BASIC_AUTH;
	}

}
