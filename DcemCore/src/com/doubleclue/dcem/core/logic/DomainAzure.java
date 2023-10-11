package com.doubleclue.dcem.core.logic;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.faces.context.ExternalContext;
import javax.naming.directory.Attributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.StringUtils;
//import com.doubleclue.utils.StringUtils;
import com.google.gson.JsonElement;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.MsalInteractionRequiredException;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.UserNamePasswordParameters;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.DirectoryObject;
import com.microsoft.graph.models.Group;
import com.microsoft.graph.models.User;
import com.microsoft.graph.models.UserChangePasswordParameterSet;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.DirectoryObjectCollectionWithReferencesPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.GroupCollectionPage;
import com.microsoft.graph.requests.ProfilePhotoStreamRequest;
import com.microsoft.graph.requests.UserCollectionPage;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;

import okhttp3.Request;

public class DomainAzure implements DomainApi {

	private static final Logger logger = LogManager.getLogger(DomainAzure.class);

	private static final String AZURE_STATE = "state";
	// private static final String SELECT_USER_ATTRIBUTES = "displayName, mobilePhone, id, userPrincipalName, preferredLanguage, businessPhones, otherMails,
	// onPremisesImmutableId";
	private static final String SELECT_USER_ATTRIBUTES_EXT = "displayName, mobilePhone, id, userPrincipalName, preferredLanguage, businessPhones, otherMails, onPremisesImmutableId, profilePhoto, department, country, jobTitle, manager";
	private static final String SCOPE = "https://graph.microsoft.com/.default";
	private final DomainEntity domainEntity;
	private final static Set<String> SCOPE_USER_PASSWORD = Collections.singleton("");

	private static final int AZURE_PAGE_SIZE_999 = 999; // AZURE have a mximum of 999

	private GraphServiceClient<Request> graphServiceClient = null;
	private Date accessTokenExpires;
	private ConfidentialClientApplication confidentialClientApplication;
	PublicClientApplication publicClientApplication;
	ClientCredentialParameters clientCredentialParam;
	private String authority = null;

	public DomainAzure(DomainEntity domainEntity) {
		this.domainEntity = domainEntity;
		authority = DcemConstants.AZURE_AUTHORITY + domainEntity.getTenantId();
	}

	@Override
	public void close() {
	}

	@Override
	public void testConnection() throws DcemException {

		try {
			confidentialClientApplication = ConfidentialClientApplication
					.builder(domainEntity.getClientId(), ClientCredentialFactory.createFromSecret(domainEntity.getClientSecret())).authority(authority).build();
			clientCredentialParam = ClientCredentialParameters.builder(Collections.singleton(SCOPE)).build();
			publicClientApplication = PublicClientApplication.builder(domainEntity.getClientId()).authority(authority)
					// .setTokenCacheAccessAspect(tokenCacheAspect)
					.build();
			graphServiceClient = null;
			getAuthResultByClientCredentials();
		} catch (Exception e) {
			// Throwable cause = e.getCause();
			// if (cause != null && cause.getClass().equals(AuthenticationException.class)) {
			// OAuthErrorResponse errorResponse = new OAuthErrorResponse(cause.getMessage());
			// String description = errorResponse.getErrorDescription();
			// int index = description.indexOf("Trace ID:");
			// if (index > -1) {
			// description = description.substring(0, index);
			// }
			// throw new DcemException(DcemErrorCodes.AZURE_DOMAIN_AUTHENTICATION_ERROR, description);
			// }
			throw new DcemException(DcemErrorCodes.AZURE_UNEXPECTED_ERROR, e.getMessage(), e);
		}
	}

	@Override
	public DcemUser verifyLogin(DcemUser dcemUser, byte[] password) throws DcemException {
		User user;
		try {
			GraphServiceClient<Request> userGraphClient = getUserGraphClient(dcemUser, new String(password, DcemConstants.UTF_8));
			// AuthenticationMethodCollectionPage au = userGraphClient.me().authentication().methods().buildRequest().get();
			user = userGraphClient.me().buildRequest().get();
			return createDcemUser(user);
		} catch (DcemException e) {
			if (e.getErrorCode() == DcemErrorCodes.AZURE_NEEDS_MFA) {
				return getUser(dcemUser.getAccountName());
			}
			throw e;
		}
	}

	// private String getAzureUsername(DcemUser dcemUser) throws DcemException {
	// return dcemUser.getShortLoginId() + "@" + getDomainName();
	// }

	@Override
	public void updateUserPassword(DcemUser dcemUser, String currentPassword, String newPassword) throws DcemException {
		GraphServiceClient<Request> userGraphClient = getUserGraphClient(dcemUser, currentPassword);
		userGraphClient.me();
		try {
			UserChangePasswordParameterSet changePasswordParameterSet = UserChangePasswordParameterSet.newBuilder().withCurrentPassword(currentPassword)
					.withNewPassword(newPassword).build();
			userGraphClient.me().changePassword(changePasswordParameterSet);
		} catch (GraphServiceException gse) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, gse.getServiceError().message);
		}
	}

	@Override
	public List<String> getUserNames(String userFilter) throws DcemException {
		DomainUsers users = getUsers(null, null, userFilter, 20);
		List<String> names = new ArrayList<>(users.getUsers().size());
		for (DcemUser dcemUser : users.getUsers()) {
			names.add(dcemUser.getShortLoginId());
		}
		return names;
	}

	@Override
	public List<DcemGroup> getGroups(String filter, int PAGE_SIZE) throws DcemException {
		List<QueryOption> options = new ArrayList<QueryOption>();
		QueryOption queryOption = null;
		if (filter == null) {
			filter = "*";
		}
		if (filter.endsWith("*")) {
			if (filter.length() > 1) {
				queryOption = new QueryOption("$filter", "startswith(displayName, '" + filter.substring(0, filter.length() - 1) + "')");
			}
		} else {
			queryOption = new QueryOption("$filter", "displayName eq '" + filter + "'");
		}
		if (queryOption != null) {
			options.add(queryOption);
		}
		GroupCollectionPage groupCollectionPage = null;
		try {
			groupCollectionPage = getSearchGraphClient().groups().buildRequest(options).select("id, displayName").get();
		} catch (GraphServiceException e) {
			if (e.getServiceError().code.equals("InvalidAuthenticationToken")) {
				graphServiceClient = null;
				groupCollectionPage = getSearchGraphClient().groups().buildRequest(options).select("id, displayName").get();
			}
		}
		Iterator<Group> iterator = groupCollectionPage.getCurrentPage().iterator();
		List<DcemGroup> groupList = new LinkedList<>();

		while (iterator.hasNext()) {
			Group group = iterator.next();
			DcemGroup dcemgroup = new DcemGroup(domainEntity, group.id, group.displayName);
			groupList.add(dcemgroup);
		}
		return groupList;
	}

	@Override
	public List<DcemGroup> getUserGroups(DcemUser dcemUser, int pagesize) throws DcemException {
		pagesize = AZURE_PAGE_SIZE_999;
		DirectoryObjectCollectionWithReferencesPage collection = null;
		List<DcemGroup> groupList = new LinkedList<>();
		String userId = dcemUser.getUserDn();
		if (userId == null) {
			userId = dcemUser.getAccountName();
		}
		try {
			collection = getSearchGraphClient().users(userId).memberOf().buildRequest().get();
		} catch (GraphServiceException e) {
			if (e.getServiceError().code.equals("InvalidAuthenticationToken")) {
				graphServiceClient = null;
				collection = getSearchGraphClient().users().byId(dcemUser.getUserDn()).memberOf().buildRequest().get();
			} else if (e.getServiceError().code.equals("Authorization_RequestDenied")) {
				logger.info("AZURE Authorization_RequestDenied " + e.toString());
			} else {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
			}
		}
		if (collection != null) {
			Iterator<DirectoryObject> iterator = collection.getCurrentPage().iterator();
			DcemGroup dcemGroup;
			while (iterator.hasNext()) {
				DirectoryObject directoryObject = iterator.next();
				if (directoryObject instanceof Group) {
					dcemGroup = new DcemGroup(domainEntity, directoryObject.id, ((Group) directoryObject).displayName);
					groupList.add(dcemGroup);
				}
			}
		}
		return groupList;
	}

	@Override
	public HashSet<String> getUserGroupNames(DcemUser dcemUser, String filter, int pageSize) throws DcemException {
		List<DcemGroup> groups = getUserGroups(dcemUser, pageSize);
		HashSet<String> groupSet = new HashSet<>();
		for (DcemGroup dcemGroup : groups) {
			groupSet.add(dcemGroup.getName());
		}
		return groupSet;
	}

	@Override
	public DcemUser getUser(String loginId) throws DcemException {
		try {
			String userId = URLEncoder.encode(loginId, "UTF-8");
			User user;
			try {
				user = getSearchGraphClient().users().byId(userId).buildRequest().expand("manager").select(SELECT_USER_ATTRIBUTES_EXT).get();
			} catch (GraphServiceException e) {
				if (e.getServiceError().code.equals("InvalidAuthenticationToken")) {
					logger.debug("Azure InvalidAuthenticationToken");
					graphServiceClient = null;
					user = getSearchGraphClient().users().byId(userId).buildRequest().expand("manager").select(SELECT_USER_ATTRIBUTES_EXT).get();
				} else {
					throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
				}
			}
			if (user.id == null) {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, loginId);
			}
			return createDcemUser(user);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, loginId, e);
		}
	}

	@Override
	public DomainUsers getGroupMembers(DcemGroup dcemGroup, String filter) throws DcemException {
		UserCollectionPage userCollectionPage;
		try {
			userCollectionPage = getSearchGraphClient().groups(dcemGroup.getGroupDn()).membersAsUser().buildRequest().top(AZURE_PAGE_SIZE_999).get();
		} catch (GraphServiceException e) {
			if (e.getServiceError().code.equals("InvalidAuthenticationToken")) {
				logger.debug("Azure InvalidAuthenticationToken");
				graphServiceClient = null;
				userCollectionPage = getSearchGraphClient().groups(dcemGroup.getGroupDn()).membersAsUser().buildRequest().top(AZURE_PAGE_SIZE_999).get();
			} else {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
			}
		}

		List<User> list = userCollectionPage.getCurrentPage();
		List<DcemUser> userList = new ArrayList<>(list.size());
		String startsWith = null;
		if (filter != null) {
			if (filter.endsWith("*")) {
				if (filter.length() > 1) {
					startsWith = filter.substring(0, filter.length() - 1).toLowerCase();
				}
			}
		}
		for (User user : list) {
			if (startsWith != null && user.displayName.startsWith(startsWith) == false) {
				continue;
			}
			userList.add(createDcemUser(user));
		}
		return new DomainUsers(AZURE_PAGE_SIZE_999, userCollectionPage.getNextPage() == null ? false : true, userList);
	}

	@Override
	public DomainEntity getDomainEntity() {
		return domainEntity;
	}

	private String createQueryOptionText(String function, String[] properties, String value) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for (String property : properties) {
			if (!first) {
				sb.append(" or ");
			}
			sb.append(String.format("%s(%s, '%s')", function, property, value));
			first = false;
		}
		return sb.toString();
	}

	@Override
	public DomainUsers getUsers(String tree, DcemGroup dcemGroup, String userName, int pageSize) throws DcemException {
		if (pageSize > AZURE_PAGE_SIZE_999) {
			pageSize = AZURE_PAGE_SIZE_999;
		}
		if (dcemGroup != null) {
			return getGroupMembers(dcemGroup, userName);
		}
		List<QueryOption> options = new ArrayList<QueryOption>();
		String filter = userName.replace("*", "");
		if (filter.length() > 0) {
			options.add(new QueryOption("$filter",
					createQueryOptionText("startswith", new String[] { "displayName", "givenName", "surname", "onPremisesImmutableId" }, filter)));
		}
		UserCollectionPage userCollectionPage = null;
		try {
			userCollectionPage = getSearchGraphClient().users().buildRequest(options).top(pageSize).expand("manager").select(SELECT_USER_ATTRIBUTES_EXT).get();
		} catch (GraphServiceException e) {
			if (e.getServiceError().code.equals("InvalidAuthenticationToken")) {
				logger.debug("Azure InvalidAuthenticationToken");
				graphServiceClient = null;
				userCollectionPage = getSearchGraphClient().users().buildRequest(options).top(pageSize).expand("manager").select(SELECT_USER_ATTRIBUTES_EXT)
						.get();
			} else {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString());
			}
		}
		// System.out.println("DomainAzure.getUsers() NextPage: " + userCollectionPage.getNextPage());
		// System.out.println("DomainAzure.getUsers() " + userCollectionPage.additionalDataManager());
		Iterator<User> iterator = userCollectionPage.getCurrentPage().iterator();
		List<DcemUser> userList = new LinkedList<>();

		while (iterator.hasNext()) {
			User user = iterator.next();
			// AuthenticationMethodCollectionPage authenticationMethodCollectionPage =
			// getSearchGraphClient().users(user.id).authentication().methods().buildRequest().get();
			DcemUser dcemUser = createDcemUser(user);
			userList.add(dcemUser);
		}

		return new DomainUsers(AZURE_PAGE_SIZE_999, userCollectionPage.getNextPage() == null ? false : true, userList);
	}

	@Override
	public byte[] getUserPhoto(DcemUser dcemUser) throws DcemException {
		try {
			ProfilePhotoStreamRequest request = (ProfilePhotoStreamRequest) getSearchGraphClient().users(dcemUser.getUserPrincipalName()).photo().content()
					.buildRequest();
			InputStream inputStream = request.get();
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			KaraUtils.copyStream(inputStream, arrayOutputStream);
			byte[] photo = arrayOutputStream.toByteArray();
			photo = DcemUtils.resizeImage(photo, DcemConstants.PHOTO_MAX);
			return photo;
		} catch (GraphServiceException gse) {
			// if (gse.getServiceError().code.equals("ImageNotFound")) {
			// return null;
			// }
			// throw new DcemException(DcemErrorCodes.AZURE_UNEXPECTED_ERROR, "Phone", gse);
			logger.info("Couldn't retrieve photo for  " + dcemUser.getDisplayNameOrLoginId(), gse.toString());
			return null;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.AZURE_UNEXPECTED_ERROR, "Phone", e);
		}
	}

	// private DcemUser createDcemUser(JsonObject jsonObject) {
	// String userPrincipalName = jsonObject.get("userPrincipalName").getAsString();
	// DcemUser dcemUser = new DcemUser(domainEntity, jsonObject.get("id").getAsString(), userPrincipalName);
	// dcemUser.setDisplayName(jsonObject.get("displayName").getAsString());
	// dcemUser.setEmail((jsonObject.get("mail").isJsonNull()) ? null : jsonObject.get("mail").getAsString());
	// dcemUser.setMobileNumber((jsonObject.get("mobile") == null) ? null : jsonObject.get("mobile").getAsString());
	// JsonArray tele = jsonObject.get("businessPhones").getAsJsonArray();
	// if (tele.size() > 0) {
	// dcemUser.setTelephoneNumber(tele.get(0).getAsString());
	// }
	// return dcemUser;
	// }

	private DcemUser createDcemUser(User user) throws DcemException {
		DcemUser dcemUser = new DcemUser(domainEntity, user.id, user.userPrincipalName);
		DcemLdapAttributes dcemLdapAttributes = new DcemLdapAttributes();
		dcemUser.setUserPrincipalName(user.userPrincipalName);
		dcemLdapAttributes.setUserPrincipalName(user.userPrincipalName);

		dcemUser.setDisplayName(user.displayName);
		dcemLdapAttributes.setDisplayName(user.displayName);

		dcemUser.setMobileNumber(user.mobilePhone);
		dcemLdapAttributes.setMobile(user.mobilePhone);

		dcemLdapAttributes.setDn(user.id);
		if (user.businessPhones.size() > 0) {
			dcemUser.setTelephoneNumber(user.businessPhones.get(0));
			dcemLdapAttributes.setTelephone(user.businessPhones.get(0));
		}
		// user.otherMails.size();
		dcemUser.setEmail(user.userPrincipalName);
		dcemLdapAttributes.setEmail(user.userPrincipalName);
		if (user.preferredLanguage == null) {
			dcemUser.setLanguage(null);
		} else {
			Locale locale = Locale.forLanguageTag(user.preferredLanguage);
			dcemUser.setLanguage(SupportedLanguage.fromLocale(locale));
		}
		String countryCode = DcemUtils.getCountryCode(user.country);
		dcemLdapAttributes.setCountry(countryCode);
		dcemLdapAttributes.setJobTitle(user.jobTitle);
		dcemLdapAttributes.setDepartment(user.department);
		DirectoryObject manager = user.manager;
		if (manager != null) {
			dcemLdapAttributes.setManagerId(manager.id);
		}
		// System.out.println("DomainAzure.createDcemUser() " + dcemLdapAttributes.toString() + " Manager " + manager);
		dcemUser.setDcemLdapAttributes(dcemLdapAttributes);
		return dcemUser;
	}

	@Override
	public List<String> getSelectedLdapTree(String treeFilter, int pAGE_SIZE) throws DcemException {
		return null;
	}

	private GraphServiceClient<Request> getSearchGraphClient() throws DcemException {
		if (graphServiceClient == null || (accessTokenExpires != null && accessTokenExpires.before(new Date()))) {
			try {
				CompletableFuture<IAuthenticationResult> future = confidentialClientApplication.acquireToken(clientCredentialParam);
				IAuthenticationResult auth = future.get();
				String accessToken = auth.accessToken();
				accessTokenExpires = auth.expiresOnDate();
				logger.info ("Azure AccessToken expires on " + accessTokenExpires);
				graphServiceClient = getGraphServiceClient(accessToken);
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.AZURE_DOMAIN_NOT_AUTHORISED, "Could not create a graph client: " + e.getLocalizedMessage());
			}
		}
		return graphServiceClient;
	}

	private GraphServiceClient<Request> getUserGraphClient(DcemUser dcemUser, String password) throws DcemException {

		String userAccessToken = null;
		try {
			UserNamePasswordParameters userNamePasswordParameters = UserNamePasswordParameters
					.builder(SCOPE_USER_PASSWORD, dcemUser.getAccountName(), password.toCharArray()).build();
			CompletableFuture<IAuthenticationResult> acquireToken = publicClientApplication.acquireToken(userNamePasswordParameters);
			IAuthenticationResult authenticationResult = acquireToken.join();
			userAccessToken = authenticationResult.accessToken();
			return getGraphServiceClient(userAccessToken);
		} catch (Exception e) {
			if (e.getCause() instanceof MsalInteractionRequiredException && e.getCause().getMessage().startsWith("AADSTS50076")) {
				StringUtils.wipeString(password);
				throw new DcemException(DcemErrorCodes.AZURE_NEEDS_MFA, dcemUser.getAccountName());
			}
			throw new DcemException(DcemErrorCodes.DOMAIN_WRONG_AUTHENTICATION, e.toString());
		}

	}

	private GraphServiceClient<Request> getGraphServiceClient(String accessToken) {
		try {
			MsGraphSimpleAuthProvider simpleAuthProvider = new MsGraphSimpleAuthProvider(accessToken);
			GraphServiceClient<Request> graphServiceClient = GraphServiceClient.builder().authenticationProvider(simpleAuthProvider).buildClient();
			graphServiceClient.getLogger().setLoggingLevel(LoggerLevel.ERROR);
			return graphServiceClient;
		} catch (Exception e) {
			throw new Error("Could not create a graph client: " + e.getLocalizedMessage());
		}
	}

	@Override
	public Map<String, String> getUserAttributes(DcemUser dcemUser, List<String> attributeList) throws DcemException {
		try {

			// https://graph.microsoft.com/v1.0/users/{id |
			// userPrincipalName}?$select=displayName,givenName,postalCode
			StringBuffer sb = new StringBuffer();
			sb.append("/users/");
			sb.append(dcemUser.getUserDn());
			sb.append("?$select=");
			boolean firstOne = true;
			for (String attr : attributeList) {
				if (firstOne == false) {
					sb.append(',');
				}
				sb.append(attr);
				firstOne = false;
			}
			JsonElement jsonElement = getSearchGraphClient().customRequest(sb.toString()).buildRequest().get();
			Map<String, String> result = new HashMap<>();
			for (String name : attributeList) {
				// JsonElement value = jsonElement.get(name);
				if (jsonElement != null) {
					result.put(name, jsonElement.getAsString());
				}
			}
			return result;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, dcemUser.getLoginId(), e);
		}
	}

	@Override
	public void resetUserPassword(DcemUser dcemUser, String newPassword) throws DcemException {
		// IGraphServiceClient userGraphClient = getSearchGraphClient();
		//
		// userGraphClient.users(dcemUser.getLoginId()).authentication().passwordMethods("{"+dcemUser.getUserDn()+"}")
		// .resetPassword(AuthenticationMethodResetPasswordParameterSet
		// .newBuilder()
		// .withNewPassword(newPassword)
		// .withRequireChangeOnNextSignIn(null)
		// .build())
		// .buildRequest()
		// .post();
	}

	@Override
	public void changeUserPhotoProfile(DcemUser dcemUser, byte[] photo, String password) throws DcemException {
		byte[] stream = Base64.getDecoder().decode(photo);
		getSearchGraphClient().users().byId(dcemUser.getUserDn()).photo().content().buildRequest().put(stream);
	}

	private IAuthenticationResult getAuthResultByClientCredentials() throws Exception {
		ConfidentialClientApplication confidentialClientApplication = ConfidentialClientApplication
				.builder(domainEntity.getClientId(), ClientCredentialFactory.createFromSecret(domainEntity.getClientSecret())).authority(authority).build();
		ClientCredentialParameters clientCredentialParam = ClientCredentialParameters.builder(Collections.singleton(SCOPE)).build();
		CompletableFuture<IAuthenticationResult> future = confidentialClientApplication.acquireToken(clientCredentialParam);
		IAuthenticationResult authenticationResult = future.get();
		return authenticationResult;
	}

	public void sendAuthRedirect(ConnectionServicesType connectionServicesType) throws Exception {
		// state parameter to validate response from Authorization server and nonce parameter to validate idToken
		ExternalContext ec = JsfUtils.getExternalContext();
		HttpServletRequest httpRequest = (HttpServletRequest) ec.getRequest();
		HttpServletResponse httpResponse = (HttpServletResponse) ec.getResponse();
		String state = UUID.randomUUID().toString();
		String nonce = UUID.randomUUID().toString();
		CookieHelper.setStateNonceCookies(httpRequest, httpResponse, state, nonce);
		httpResponse.setStatus(302);
		String redirectUrl = getRedirectUrl(httpRequest.getParameter("claims"), connectionServicesType, state, nonce);
		if (logger.isDebugEnabled()) {
			logger.debug("Azure Redirect URL" + redirectUrl);
		}
		ec.redirect(redirectUrl);
	}

	private String getRedirectUrl(String claims, ConnectionServicesType connectionServicesType, String state, String nonce) throws Exception {
		DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
		String url = applicationBean.getServiceUrl(connectionServicesType);
		String redirectUrl = authority + "/oauth2/v2.0/authorize?response_type=code&response_mode=form_post&redirect_uri=" + URLEncoder.encode(url, "UTF-8")
				+ "&client_id=" + domainEntity.getClientId() + "&scope=" + URLEncoder.encode("openid offline_access profile", "UTF-8")
				+ (org.apache.commons.lang3.StringUtils.isEmpty(claims) ? "" : "&claims=" + claims) + "&prompt=select_account&state=" + state + "&nonce="
				+ nonce;
		return redirectUrl;
	}

	/**
	* @param httpRequest
	* @param currentUri
	* @param fullUrl
	* @throws Throwable
	*/
	public DcemUser processAuthenticationCodeRedirect(HttpServletRequest httpRequest, String currentUri, String fullUrl) throws Throwable {

		Map<String, List<String>> params = new HashMap<>();
		for (String key : httpRequest.getParameterMap().keySet()) {
			params.put(key, Collections.singletonList(httpRequest.getParameterMap().get(key)[0]));
		}
		// validate that state in response equals to state in request
		String state = CookieHelper.getCookie(httpRequest, CookieHelper.MSAL_WEB_APP_STATE_COOKIE);
		String cookieValue = params.get(AZURE_STATE).get(0);
		if (state == null || state.isEmpty() || state.equals(cookieValue) == false) {
			throw new DcemException(DcemErrorCodes.MSAL_INVALID_STATE, "could not validate state");
		}
		AuthenticationResponse authResponse = AuthenticationResponseParser.parse(new URI(fullUrl), params);
		if (authResponse instanceof AuthenticationSuccessResponse) {
			// succesfull login
			AuthenticationSuccessResponse oidcResponse = (AuthenticationSuccessResponse) authResponse;
			// validate that OIDC Auth Response matches Code Flow (contains only requested artifacts)
			if (oidcResponse.getIDToken() != null || oidcResponse.getAccessToken() != null || oidcResponse.getAuthorizationCode() == null) {
				throw new DcemException(DcemErrorCodes.MSAL_FAILED_TO_VALIDATE_MESSAGE, "unexpected set of artifacts received");
			}
			IAuthenticationResult result = getAuthResultByAuthCode(httpRequest, oidcResponse.getAuthorizationCode(), currentUri);
			// validate nonce to prevent reply attacks (code maybe substituted to one with broader access)
			cookieValue = CookieHelper.getCookie(httpRequest, CookieHelper.MSAL_WEB_APP_NONCE_COOKIE);
			String nonce = (String) JWTParser.parse(result.idToken()).getJWTClaimsSet().getClaim("nonce");
			if (nonce == null || nonce.isEmpty() || nonce.equals(cookieValue) == false) {
				throw new DcemException(DcemErrorCodes.MSAL_FAILED_TO_VALIDATE_MESSAGE, "could not validate nonce");
			}
			GraphServiceClient<Request> userGraphClient = getGraphServiceClient(result.accessToken());
			User user = userGraphClient.me().buildRequest().get();
			return createDcemUser(user);
		} else {
			AuthenticationErrorResponse oidcResponse = (AuthenticationErrorResponse) authResponse;
			throw new DcemException(DcemErrorCodes.MSAL_LOGIN_FAILED, String.format("Request for auth code failed: %s - %s",
					oidcResponse.getErrorObject().getCode(), oidcResponse.getErrorObject().getDescription()));
		}
	}

	IAuthenticationResult getAuthResultByAuthCode(HttpServletRequest httpServletRequest, AuthorizationCode authorizationCode, String currentUri)
			throws Throwable {

		IAuthenticationResult result;
		try {
			AuthorizationCodeParameters parameters = AuthorizationCodeParameters.builder(authorizationCode.getValue(), new URI(currentUri)).build();
			Future<IAuthenticationResult> future = confidentialClientApplication.acquireToken(parameters);
			result = future.get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
		if (result == null) {
			throw new DcemException(DcemErrorCodes.MSAL_AUTH_NO_RESULT, "authentication result was null");
		}
		// TODO httpServletRequest.getSession().setAttribute(AuthHelper.TOKEN_CACHE_SESSION_ATTRIBUTE, tokenCache);
		// result.account().username();
		return result;
	}

	// this is not supported by azure
	@Override
	public Map<String, Attributes> customSearchAttributeMap(String tree, String searchFilter, String baseDn, String[] returnedAttributes, int pageSize)
			throws DcemException {
		// TODO Auto-generated method stub
		return null;
	}

}
