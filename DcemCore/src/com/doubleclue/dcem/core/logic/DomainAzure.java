package com.doubleclue.dcem.core.logic;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.AzureUtils;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.oauth.oauth2.OAuthErrorResponse;
import com.doubleclue.utils.KaraUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.aad.adal4j.AuthenticationException;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.extensions.DirectoryObject;
import com.microsoft.graph.models.extensions.Group;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IDirectoryObjectCollectionWithReferencesPage;
import com.microsoft.graph.requests.extensions.IGroupCollectionPage;
import com.microsoft.graph.requests.extensions.IUserCollectionPage;
import com.microsoft.graph.requests.extensions.ProfilePhotoStreamRequest;

public class DomainAzure implements DomainApi {
	
	private static final Logger logger = LogManager.getLogger(DomainAzure.class);

	private static final String SELECT_USER_ATTRIBUTES = "displayName, mobilePhone, id, userPrincipalName, preferredLanguage, businessPhones, otherMails, onPremisesImmutableId";
	private static final String SELECT_USER_ATTRIBUTES_EXT = "displayName, mobilePhone, id, userPrincipalName, preferredLanguage, businessPhones, otherMails, onPremisesImmutableId, profilePhoto";

	private final DomainEntity domainEntity;

	private IGraphServiceClient graphClient = null;
	private String domainName = null;

	public DomainAzure(DomainEntity domainEntity) {
		this.domainEntity = domainEntity;
	}

	@Override
	public void close() {
	}

	@Override
	public void testConnection() throws DcemException {
		try {
			AzureUtils.getAuthResultByClientCredentials(domainEntity);
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause != null && cause.getClass().equals(AuthenticationException.class)) {
				OAuthErrorResponse errorResponse = new OAuthErrorResponse(cause.getMessage());
				String description = errorResponse.getErrorDescription();
				int index = description.indexOf("Trace ID:");
				if (index > -1) {
					description = description.substring(0, index);
				}
				throw new DcemException(DcemErrorCodes.AZURE_DOMAIN_AUTHENTICATION_ERROR, description);
			}
			throw new DcemException(DcemErrorCodes.AZURE_UNEXPECTED_ERROR, e.getMessage());
		}
	}

	@Override
	public DcemUser verifyLogin(DcemUser dcemUser, byte[] password) throws DcemException {
		IGraphServiceClient userGraphClient = getUserGraphClient(dcemUser, new String(password, DcemConstants.UTF_8));
		User user = userGraphClient.me().buildRequest().select(SELECT_USER_ATTRIBUTES).get();
		return createDcemUser(user);
	}

	// private String getAzureUsername(DcemUser dcemUser) throws DcemException {
	// return dcemUser.getShortLoginId() + "@" + getDomainName();
	// }

	@Override
	public void updateUserPassword(DcemUser dcemUser, String currentPassword, String newPassword) throws DcemException {
		IGraphServiceClient userGraphClient = getUserGraphClient(dcemUser, currentPassword);
		try {
			userGraphClient.me().changePassword(currentPassword, newPassword).buildRequest().post();
		} catch (GraphServiceException gse) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, gse.getServiceError().message);
		}
	}

	@Override
	public List<String> getUserNames(String userFilter) throws DcemException {
		List<DcemUser> users = getUsers(null, null, userFilter, 20);
		List<String> names = new ArrayList<>(users.size());
		for (DcemUser dcemUser : users) {
			names.add(dcemUser.getShortLoginId());
		}
		return names;
	}

	@Override
	public List<DcemGroup> getGroups(String filter, int PAGE_SIZE) throws DcemException {
		List<QueryOption> options = new ArrayList<QueryOption>();
		QueryOption queryOption = null;
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
		IGroupCollectionPage groupCollectionPage = null;
		try {
			groupCollectionPage = getSearchGraphClient().groups().buildRequest(options).select("id, displayName").get();
		} catch (GraphServiceException e) {
			if (e.getServiceError().code.equals("InvalidAuthenticationToken")) {
				graphClient = null;
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
		IDirectoryObjectCollectionWithReferencesPage collection;
		try {
			collection = getSearchGraphClient().users().byId(dcemUser.getUserDn()).memberOf().buildRequest().select("id, displayName").get();
		} catch (GraphServiceException e) {
			if (e.getServiceError().code.equals("InvalidAuthenticationToken")) {
				graphClient = null;
				collection = getSearchGraphClient().users().byId(dcemUser.getUserDn()).memberOf().buildRequest().select("id, displayName").get();
			} else {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
			}
		}
		Iterator<DirectoryObject> iterator = collection.getCurrentPage().iterator();
		DcemGroup dcemGroup;
		List<DcemGroup> groupList = new LinkedList<>();
		while (iterator.hasNext()) {
			DirectoryObject directoryObject = iterator.next();
			dcemGroup = new DcemGroup(domainEntity, directoryObject.id, directoryObject.getRawObject().get("displayName").getAsString());
			groupList.add(dcemGroup);
		}
		return groupList;
	}

	@Override
	public HashSet<String> getUserGroupNames(DcemUser dcemUser, String filter, int pageSize) throws DcemException {
		IDirectoryObjectCollectionWithReferencesPage collection;
		try {
			String userId = dcemUser.getUserDn();
			if (userId == null) {
				userId = dcemUser.getAccountName();
			}
			collection = getSearchGraphClient().users().byId(userId).memberOf().buildRequest().top(pageSize).select("displayName").get();
		} catch (GraphServiceException e) {
			if (e.getServiceError().code.equals("InvalidAuthenticationToken")) {
				graphClient = null;
				collection = getSearchGraphClient().users().byId(dcemUser.getUserDn()).memberOf().buildRequest().top(pageSize).select("displayName").get();
			} else {
		//		throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
				return new HashSet<>();
			}
		}

		Iterator<DirectoryObject> iterator = collection.getCurrentPage().iterator();
		HashSet<String> groupSet = new HashSet<>();
		while (iterator.hasNext()) {
			DirectoryObject directoryObject = iterator.next();
			groupSet.add(domainEntity.getName() + DcemConstants.DOMAIN_SEPERATOR + directoryObject.getRawObject().get("displayName").getAsString());
		}
		return groupSet;
	}

	@Override
	public DcemUser getUser(String loginId) throws DcemException {
		try {
			String userId = URLEncoder.encode(loginId, "UTF-8");
			User user = getSearchGraphClient().users().byId(userId).buildRequest().get();
			if (user.id == null) {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, loginId);
			}
			return createDcemUser(user);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, loginId, e);
		}

	}

	@Override
	public List<DcemUser> getGroupMembers(DcemGroup dcemGroup, String filter) throws DcemException {

		IDirectoryObjectCollectionWithReferencesPage collectionWithReferencesPage;
		try {
			collectionWithReferencesPage = getSearchGraphClient().groups(dcemGroup.getGroupDn()).members().buildRequest().get();
		} catch (GraphServiceException e) {
			if (e.getServiceError().code.equals("InvalidAuthenticationToken")) {
				graphClient = null;
				collectionWithReferencesPage = getSearchGraphClient().groups(dcemGroup.getGroupDn()).members().buildRequest().get();
			} else {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
			}
		}

		List<DirectoryObject> list = collectionWithReferencesPage.getCurrentPage();
		List<DcemUser> userList = new ArrayList<>(list.size());
		String startsWith = null;
		if (filter != null) {
			if (filter.endsWith("*")) {
				if (filter.length() > 1) {
					startsWith = filter.substring(0, filter.length() - 1).toLowerCase();
				}
			}
		}
		for (DirectoryObject directoryObject : list) {
			if (startsWith != null) {
				String displayName = directoryObject.getRawObject().get("displayName").getAsString().toLowerCase();
				if (displayName.startsWith(startsWith) == false) {
					continue;
				}
			}
			userList.add(createDcemUser(directoryObject.getRawObject()));
		}
		return userList;
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
	public List<DcemUser> getUsers(String tree, DcemGroup dcemGroup, String userName, int pageSize) throws DcemException {
		if (dcemGroup != null) {
			return getGroupMembers(dcemGroup, userName);
		}
		List<QueryOption> options = new ArrayList<QueryOption>();
		String filter = userName.replace("*", "");
		if (filter.length() > 0) {
			options.add(new QueryOption("$filter",
					createQueryOptionText("startswith", new String[] { "displayName", "givenName", "surname", "onPremisesImmutableId" }, filter)));
		}
		// if (groupDn != null) {
		// queryOption = new QueryOption("$filter", "memberOf eq '" + groupDn + "'");
		// options.add(queryOption);
		// }
		IUserCollectionPage userCollectionPage = null;
		try {
			userCollectionPage = getSearchGraphClient().users().buildRequest(options).top(pageSize).select(SELECT_USER_ATTRIBUTES_EXT).get();
		} catch (GraphServiceException e) {
			if (e.getServiceError().code.equals("InvalidAuthenticationToken")) {
				graphClient = null;
				userCollectionPage = getSearchGraphClient().users().buildRequest(options).top(pageSize).select(SELECT_USER_ATTRIBUTES_EXT).get();
			} else {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString());
			}
		}
		Iterator<User> iterator = userCollectionPage.getCurrentPage().iterator();
		List<DcemUser> userList = new LinkedList<>();

		while (iterator.hasNext()) {
			User user = iterator.next();
			DcemUser dcemUser = createDcemUser(user);
			userList.add(dcemUser);
		}
		return userList;
	}

	@Override
	public byte[] getUserPhoto(DcemUser dcemUser) throws DcemException {
		try {
			ProfilePhotoStreamRequest request = (ProfilePhotoStreamRequest) getSearchGraphClient().users(dcemUser.getUserPrincipalName()).photo().content()
					.buildRequest();
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			InputStream inputStream = request.get();
			KaraUtils.copyStream(inputStream, arrayOutputStream);
			byte[] photo = arrayOutputStream.toByteArray();
			photo = DcemUtils.resizeImage(photo);
			return photo;
		} catch (GraphServiceException gse) {
//			if (gse.getServiceError().code.equals("ImageNotFound")) {
//				return null;
//			}
//			throw new DcemException(DcemErrorCodes.AZURE_UNEXPECTED_ERROR, "Phone", gse);
			logger.debug("Couldn't retrieve photo for  " + dcemUser.getDisplayNameOrLoginId(), gse.toString());
			return null;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.AZURE_UNEXPECTED_ERROR, "Phone", e);
		}
	}

	private DcemUser createDcemUser(JsonObject jsonObject) {
		String userPrincipalName = jsonObject.get("userPrincipalName").getAsString();
		DcemUser dcemUser = new DcemUser(domainEntity, jsonObject.get("id").getAsString(), userPrincipalName);
		dcemUser.setDisplayName(jsonObject.get("displayName").getAsString());
		dcemUser.setEmail((jsonObject.get("mail").isJsonNull()) ? null : jsonObject.get("mail").getAsString());
		dcemUser.setMobileNumber((jsonObject.get("mobile") == null) ? null : jsonObject.get("mobile").getAsString());
		JsonArray tele = jsonObject.get("businessPhones").getAsJsonArray();
		if (tele.size() > 0) {
			dcemUser.setTelephoneNumber(tele.get(0).getAsString());
		}
		return dcemUser;
	}

	private DcemUser createDcemUser(User user) {
		DcemUser dcemUser = new DcemUser(domainEntity, user.id, user.userPrincipalName);
		DcemLdapAttributes dcemLdapAttributes = new DcemLdapAttributes();
		dcemUser.setUserPrincipalName(user.userPrincipalName);
		dcemLdapAttributes.setUserPrincipalName(user.userPrincipalName);

		dcemUser.setDisplayName(user.displayName);
		dcemLdapAttributes.setDisplayName(user.displayName);

		dcemUser.setMobileNumber(user.mobilePhone);
		if (user.businessPhones.size() > 0) {
			dcemUser.setTelephoneNumber(user.businessPhones.get(0));
			dcemLdapAttributes.setTelephone(user.businessPhones.get(0));
		}
		if (user.memberOf != null) {
			System.out.println("DomainAzure.getUsers()");
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
		dcemUser.setDcemLdapAttributes(dcemLdapAttributes);
		return dcemUser;
	}

	@Override
	public List<String> getSelectedLdapTree(String treeFilter, int pAGE_SIZE) throws DcemException {
		return null;
	}

	private IGraphServiceClient getSearchGraphClient() throws DcemException {
		if (graphClient == null) {
			try {
				String token = AzureUtils.getAuthResultByClientCredentials(domainEntity).getAccessToken();
				graphClient = getGraphClient(token);
				getSearchGraphClient().getLogger().setLoggingLevel(LoggerLevel.ERROR);
			} catch (DcemException e) {
				throw e;
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.AZURE_DOMAIN_NOT_AUTHORISED, "Could not create a graph client: " + e.getLocalizedMessage());
			}
		}
		return graphClient;
	}

	// private String getDomainName() throws DcemException {
	// if (domainName == null) {
	// try {
	// Organization org = getSearchGraphClient().organization(AzureUtils.getTenantID(domainEntity))
	// .buildRequest().get();
	// List<VerifiedDomain> domains = org.verifiedDomains;
	// for (VerifiedDomain domain : domains) {
	// if (domains.size() == 1 || domain.isDefault || domain.isInitial) {
	// domainName = domain.name;
	// }
	// }
	// } catch (DcemException e) {
	// throw e;
	// } catch (Exception e) {
	// throw new DcemException(DcemErrorCodes.AZURE_DOMAIN_NOT_AUTHORISED,
	// "Could not get azure domain name: " + e.getMessage());
	// }
	// }
	// return domainName;
	// }

	private IGraphServiceClient getUserGraphClient(DcemUser dcemUser, String password) throws DcemException {
		String userAccessToken = AzureUtils.getAuthResultByRopc(domainEntity, dcemUser.getUserPrincipalName(), password).getAccessToken();
		return getGraphClient(userAccessToken);
	}

	private IGraphServiceClient getGraphClient(String accessToken) {
		try {
			IAuthenticationProvider mAuthenticationProvider = new IAuthenticationProvider() {
				// @Override
				public void authenticateRequest(final IHttpRequest request) {
					request.addHeader("Authorization", "Bearer " + accessToken);
				}
			};
			IClientConfig mClientConfig = DefaultClientConfig.createWithAuthenticationProvider(mAuthenticationProvider);
			return GraphServiceClient.fromConfig(mClientConfig);
		} catch (Exception e) {
			throw new Error("Could not create a graph client: " + e.getLocalizedMessage());
		}
	}

	

	@Override
	public Map<String, String> getUserAttributes(DcemUser dcemUser, List<String> attributeList) throws DcemException {
		try {

			// https://graph.microsoft.com/v1.0/users/{id | userPrincipalName}?$select=displayName,givenName,postalCode
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
			JsonObject jsonObject = getSearchGraphClient().customRequest(sb.toString()).buildRequest().get();
			Map<String, String> result = new HashMap<>();
			for (String name : attributeList) {
				JsonElement value = jsonObject.get(name);
				if (value != null) {
					result.put(name, value.getAsString());
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
		IGraphServiceClient userGraphClient = getSearchGraphClient();
		byte[] stream = Base64.getDecoder().decode(photo);
		userGraphClient.users().byId(dcemUser.getUserDn()).photo().content().buildRequest().put(stream);
	}

	// private String getAccessTokenForUser(DcemUser dcemUser, String password) throws DcemException {
	// try {
	// return AzureUtils.getAuthResultByRopc(domainEntity, dcemUser.getEmail(), password).getAccessToken();
	// } catch (Exception e) {
	// throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Error while obtaining Access Token for user '" + dcemUser.getEmail() + "' : " +
	// e.getMessage());
	// }
	// }

	// private String getUsernameFromPrincipalName(String principalName) {
	// return principalName.substring(0, principalName.lastIndexOf('@'));
	// }
}
