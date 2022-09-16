
package com.doubleclue.dcem.admin.windowssso;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import waffle.servlet.AutoDisposableWindowsPrincipal;
import waffle.servlet.WindowsPrincipal;
import waffle.servlet.spi.SecurityFilterProviderCollection;
import waffle.util.AuthorizationHeader;
import waffle.windows.auth.IWindowsAuthProvider;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsImpersonationContext;
import waffle.windows.auth.PrincipalFormat;
import waffle.windows.auth.impl.WindowsAuthProviderImpl;

@Named("windowsSso")
@ApplicationScoped
public class WindowsSso  {
	

	/** The Constant PRINCIPALSESSIONKEY. */
//	private static final String PRINCIPALSESSIONKEY = NegotiateSecurityFilter.class.getName() + ".PRINCIPAL";

	/** The windows flag. */
	private boolean isWindows;

	/** The exclusion bearer authorization. */
//	private boolean excludeBearerAuthorization;

	/** The providers. */
	private SecurityFilterProviderCollection providers;

	/** The auth. */
	private IWindowsAuthProvider authProvider = new WindowsAuthProviderImpl();

	/** The allow guest login. */
	private boolean allowGuestLogin = false;

	/** The impersonate. */
	private boolean impersonate;

	/** The role format. */
	private PrincipalFormat roleFormat = PrincipalFormat.FQN;

	/** The principal format. */
	private PrincipalFormat principalFormat = PrincipalFormat.FQN;

	/**
	 * 
	 */
	private static final Logger logger = LogManager.getLogger(WindowsSso.class);

	// private AsClientRestApi clientRestApi = AsClientRestApi.getInstance();

	@PostConstruct
	public void init() {
		String os = System.getProperty("os.name").toLowerCase();
		isWindows = os.contains("win");
		try {
			this.providers = new SecurityFilterProviderCollection(this.authProvider);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		System.out.println("SsoServlet.init()");
	}

	/**
	 * @param sreq
	 * @param sres
	 * @param chain
	 * @throws IOException
	 * @throws ServletException
	 */
	public WindowsSsoResult singleSignOn (HttpServletRequest request, HttpServletResponse response)	throws Exception {

		if (isWindows == false) {
			return new WindowsSsoResult(WindowsSsoResultType.NON_WINDOWS);
		}			
		final AuthorizationHeader authorizationHeader = new AuthorizationHeader(request);
		logger.debug ("authorization Header: " + request.getHeader("authorization"));
		if (authorizationHeader.isNull() == false) {
			// log the user in using the token
			IWindowsIdentity windowsIdentity;
			try {
				windowsIdentity = this.providers.doFilter(request, response);
				if (windowsIdentity == null) {
					return new WindowsSsoResult(WindowsSsoResultType.NO_WINDOWS_PROVIDER);
				}
			} catch (final IOException e) {
				logger.info("error logging in user: {}", e.getMessage());
				logger.trace("", e);
				return new WindowsSsoResult(WindowsSsoResultType.EXCEPTION);
			}

			IWindowsImpersonationContext ctx = null;
			try {
				if (this.allowGuestLogin == false && windowsIdentity.isGuest()) {
					logger.warn("guest login disabled: {}", windowsIdentity.getFqn());
					return new WindowsSsoResult(WindowsSsoResultType.NO_GUEST_ALLOWED);
				}
//				WindowsPrincipal windowsPrincipal;
//				if (this.impersonate) {
//					windowsPrincipal = new AutoDisposableWindowsPrincipal(windowsIdentity, this.principalFormat,
//							this.roleFormat);
//				} else {
//					windowsPrincipal = new WindowsPrincipal(windowsIdentity, this.principalFormat, this.roleFormat);
//				}
				logger.info("successfully logged in user: {}", windowsIdentity.getFqn());
				WindowsSsoResult windowsSsoResult = new WindowsSsoResult(WindowsSsoResultType.OK);
				windowsSsoResult.setFqn(windowsIdentity.getFqn());
				windowsSsoResult.setSid(windowsIdentity.getSid());
				return windowsSsoResult;
			} finally {
				if (this.impersonate && ctx != null) {
					logger.debug("terminating impersonation");
					ctx.revertToSelf();
				} else {
					windowsIdentity.dispose();
				}
			}
		}
		return new WindowsSsoResult(WindowsSsoResultType.NO_AUTHORIZATION_HEADER);
	}

	/**
	 * Filter for a previously logged on user.
	 *
	 * @param request  HTTP request.
	 * @param response HTTP response.
	 * @param chain    Filter chain.
	 * @return True if a user already authenticated.
	 * @throws IOException      Signals that an I/O exception has occurred.
	 * @throws ServletException the servlet exception
	 */
//	private boolean doFilterPrincipal(final HttpServletRequest request, final HttpServletResponse response)
//			throws IOException, ServletException {
//		Principal principal = request.getUserPrincipal();
//		if (principal == null) {
//			final HttpSession session = request.getSession(false);
//			if (session != null) {
//				principal = (Principal) session.getAttribute(PRINCIPALSESSIONKEY);
//			} 
//		}
//		if (principal == null) {
//			// no principal in this request
//			return false;
//		}
//		if (this.providers.isPrincipalException(request)) {
//			// the providers signal to authenticate despite an existing principal, eg. NTLM
//			// post
//			return false;
//		}
//
//		// user already authenticated
//		if (principal instanceof WindowsPrincipal) {
//			logger.debug("previously authenticated Windows user: {}", principal.getName());
//			final WindowsPrincipal windowsPrincipal = (WindowsPrincipal) principal;
//
//			if (this.impersonate && windowsPrincipal.getIdentity() == null) {
//				// This can happen when the session has been serialized then de-serialized
//				// and because the IWindowsIdentity field is transient. In this case re-ask an
//				// authentication to get a new identity.
//				return false;
//			}
//
//			final NegotiateRequestWrapper requestWrapper = new NegotiateRequestWrapper(request, windowsPrincipal);
//
//			IWindowsImpersonationContext ctx = null;
//			if (this.impersonate) {
//				logger.debug("re-impersonating user");
//				ctx = windowsPrincipal.getIdentity().impersonate();
//			}
//			try {
//				chain.doFilter(requestWrapper, response);
//			} finally {
//				if (this.impersonate && ctx != null) {
//					logger.debug("terminating impersonation");
//					ctx.revertToSelf();
//				}
//			}
//		} else {
//			logger.debug("previously authenticated user: {}", principal.getName());
//			chain.doFilter(request, response);
//		}
//		return true;
//	}

	/**
	 * Send a 401 Unauthorized along with protocol authentication headers.
	 *
	 * @param response HTTP Response
	 * @param close    Close connection.
	 */
	public void sendUnauthorized(final HttpServletResponse response, final boolean close) {
		try {
			this.providers.sendUnauthorized(response);
			if (close) {
				response.setHeader("Connection", "close");
			} else {
				response.setHeader("Connection", "keep-alive");
			}
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			response.flushBuffer();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean isWindows() {
		return isWindows;
	}
}
