package com.doubleclue.as.restapi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericType;

import com.doubleclue.as.restapi.auth.HttpBasicAuth;
import com.doubleclue.as.restapi.model.AddMessageResponse;
import com.doubleclue.as.restapi.model.AsApiActivationCode;
import com.doubleclue.as.restapi.model.AsApiAuthMethod;
import com.doubleclue.as.restapi.model.AsApiAuthenticateResponse;
import com.doubleclue.as.restapi.model.AsApiCloudSafeFile;
import com.doubleclue.as.restapi.model.AsApiDevice;
import com.doubleclue.as.restapi.model.DcemApiException;
import com.doubleclue.as.restapi.model.AsApiFidoAuthenticator;
import com.doubleclue.as.restapi.model.AsApiFilterItem;
import com.doubleclue.as.restapi.model.AsApiMessage;
import com.doubleclue.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.as.restapi.model.AsApiOtpToken;
import com.doubleclue.as.restapi.model.AsApiShareCloudSafe;
import com.doubleclue.as.restapi.model.AsApiShareCloudSafeDetails;
import com.doubleclue.as.restapi.model.AsApiUPGuiConfig;
import com.doubleclue.as.restapi.model.AsApiUrlToken;
import com.doubleclue.as.restapi.model.AsApiUser;
import com.doubleclue.as.restapi.model.QueryLoginResponse;
import com.doubleclue.as.restapi.model.RequestLoginQrCodeResponse;

public class AsClientRestApi {

	private ApiClient apiClient;

	private String[] localVarAuthNames = new String[] { HttpBasicAuth.class.getSimpleName() };

	private final String[] localVarAccepts = { "application/json" };
	private String localVarAccept;

	private final String[] localVarContentTypes = { "application/json" };
	private String localVarContentType;

	private static AsClientRestApi asClientRestApi;

	public static AsClientRestApi initilize(RestConnectionConfig connectionConfig) {
		asClientRestApi = new AsClientRestApi(connectionConfig);
		return asClientRestApi;
	}

	public static AsClientRestApi getInstance() {
		return asClientRestApi;
	}

	public AsClientRestApi(RestConnectionConfig connectionConfig) {
		apiClient = new ApiClient(connectionConfig);
		localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

	}

	// public AsClientRestApi(ApiClient apiClient) {
	// this.apiClient = apiClient;
	// }

	public ApiClient getApiClient() {
		return apiClient;
	}
	//
	// public void setApiClient(ApiClient apiClient) {
	// this.apiClient = apiClient;
	// }

	/**
	 *
	 * Creates a new message to user
	 *
	 * @param apiMessage
	 *            Message to user - device (required)
	 * @return AddMessageResponse
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public AddMessageResponse addMessage(AsApiMessage apiMessage) throws DcemApiException, ApiException {
		Object localVarPostBody = apiMessage;

		// verify the required parameter 'apiMessage' is set
		if (apiMessage == null) {
			throw new ApiException(400, "Missing the required parameter 'apiMessage' when calling addMessage");
		}

		// create path and map variables
		String localVarPath = "/message/add".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		GenericType<AddMessageResponse> localVarReturnType = new GenericType<AddMessageResponse>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Cancel a pending mesage
	 *
	 * @param msgId
	 *            message unique id (required)
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public void cancelMessage(Long msgId) throws DcemApiException, ApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'msgId' is set
		if (msgId == null) {
			throw new ApiException(400, "Missing the required parameter 'msgId' when calling cancelMessage");
		}

		// create path and map variables
		String localVarPath = "/message/cancel".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "msgId", msgId));

		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

	/**
	 *
	 * Cancal all user pending messages
	 *
	 * @param msgId
	 *            user name (required)
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public void cancelUserMessages(String msgId) throws DcemApiException, ApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'msgId' is set
		if (msgId == null) {
			throw new ApiException(400, "Missing the required parameter 'msgId' when calling cancelUserMessages");
		}

		// create path and map variables
		String localVarPath = "/message/cancelUserMessages".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "msgId", msgId));
		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

	/**
	 *
	 * This is used for testing purposes
	 *
	 * @param text
	 *            (optional)
	 * @return String
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public String echo(String text) throws DcemApiException, ApiException {
		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/misc/echo".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "text", text));
		GenericType<String> localVarReturnType = new GenericType<String>() {
		};
		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Returns all pets from the system that the user has access to
	 *
	 * @param msgId
	 *            The message Id returned by post Message (required)
	 * @return AsApiMessageResponse
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public AsApiMessageResponse getMessageResponse(Long msgId, int waitTimeSeconds) throws DcemApiException, ApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'msgId' is set
		if (msgId == null) {
			throw new ApiException(400, "Missing the required parameter 'msgId' when calling getMessageResponse");
		}

		// create path and map variables
		String localVarPath = "/message".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "msgId", msgId));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "waitTimeSeconds", waitTimeSeconds));

		GenericType<AsApiMessageResponse> localVarReturnType = new GenericType<AsApiMessageResponse>() {
		};
		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Queries if the login code was consumed
	 *
	 * @param otp
	 *            this is Login One Time Password (required)
	 * @return QueryLoginResponse
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public QueryLoginResponse queryLoginOTP(String otp) throws DcemApiException, ApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'otp' is set
		if (otp == null) {
			throw new ApiException(400, "Missing the required parameter 'otp' when calling queryLoginOTP");
		}

		// create path and map variables
		String localVarPath = "/login/queryLoginOtp".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "otp", otp));

		GenericType<QueryLoginResponse> localVarReturnType = new GenericType<QueryLoginResponse>() {
		};
		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	public QueryLoginResponse queryLoginQrCode(String sessionId) throws DcemApiException, ApiException {
		return queryLoginQrCode(sessionId, false, 0);
	}

	/**
	 *
	 * Queries if the login code was consumed
	 *
	 * @param sessionId
	 *            this is the session Id of the portal (required)
	 * @return QueryLoginQrCodeResponse
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public QueryLoginResponse queryLoginQrCode(String sessionId, boolean pollOnly, int waitTimeSeconds) throws DcemApiException, ApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'sessionId' is set
		if (sessionId == null) {
			throw new ApiException(400, "Missing the required parameter 'sessionId' when calling queryLoginCode");
		}

		// create path and map variables
		String localVarPath = "/login/queryLoginQrCode".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "sessionId", sessionId));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "pollOnly", pollOnly));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "waitTimeSeconds", waitTimeSeconds));

		GenericType<QueryLoginResponse> localVarReturnType = new GenericType<QueryLoginResponse>() {
		};
		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Get a Byte Array for Qr-Code generation
	 *
	 * @param sessionId
	 *            this is the session Id of the portal (required)
	 * @return RequestLoginQrCodeResponse
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public RequestLoginQrCodeResponse requestLoginQrCode(String sessionId) throws DcemApiException, ApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'sessionId' is set
		if (sessionId == null) {
			throw new ApiException(400, "Missing the required parameter 'sessionId' when calling requestLoginQrCode");
		}

		// create path and map variables
		String localVarPath = "/login/requestLoginQrCode".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "sessionId", sessionId));

		GenericType<RequestLoginQrCodeResponse> localVarReturnType = new GenericType<RequestLoginQrCodeResponse>() {
		};
		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Queries
	 *
	 * @param user
	 *            Message to user - device (required)
	 * @param offset
	 *            (optional)
	 * @param maxResults
	 *            (optional)
	 * @return List<AsApiUser>
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public List<AsApiUser> queryUsers(List<AsApiFilterItem> filterItems, Integer offset, Integer maxResults) throws DcemApiException, ApiException {
		Object localVarPostBody = filterItems;

		// create path and map variables
		String localVarPath = "/user/queryUsers".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxResults", maxResults));

		GenericType<List<AsApiUser>> localVarReturnType = new GenericType<List<AsApiUser>>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Add activation code to a user
	 *
	 * @param activationCode
	 *            If activationCode field is null, a new activationCode will be
	 *            generated (required)
	 * @return String
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public String addActivationCode(AsApiActivationCode activationCode) throws DcemApiException, ApiException {
		Object localVarPostBody = activationCode;

		// verify the required parameter 'activationCode' is set
		if (activationCode == null) {
			throw new ApiException(400, "Missing the required parameter 'activationCode' when calling addActivationCode");
		}

		// create path and map variables
		String localVarPath = "/user/addActivationCode".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		GenericType<String> localVarReturnType = new GenericType<String>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * get User using the loginId
	 *
	 * @param loginId
	 *            (required)
	 * @return AsApiUser
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public AsApiUser getUser(String loginId) throws DcemApiException, ApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'loginId' is set
		if (loginId == null) {
			throw new ApiException(400, "Missing the required parameter 'loginId' when calling getUser");
		}

		// create path and map variables
		String localVarPath = "/user/getUser".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "loginId", loginId));

		GenericType<AsApiUser> localVarReturnType = new GenericType<AsApiUser>() {
		};
		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Change User
	 *
	 * @param user
	 *            Data with null content will not be changed. (required)
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public void modifyUser(AsApiUser user) throws DcemApiException, ApiException {
		Object localVarPostBody = user;

		// verify the required parameter 'user' is set
		if (user == null) {
			throw new ApiException(400, "Missing the required parameter 'user' when calling modifyUser");
		}

		// create path and map variables
		String localVarPath = "/user/modifyUser".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

		
	/**
	 *
	 * Queries
	 *
	 * @param filters
	 *            Message to user - device (required)
	 * @param offset
	 *            (optional)
	 * @param maxResults
	 *            (optional)
	 * @return List<AsApiCloudData>
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public List<AsApiCloudSafeFile> queryCloudSafe(List<AsApiFilterItem> filters, int offset, int maxResults) throws DcemApiException, ApiException {

		Object localVarPostBody = filters;

		// verify the required parameter 'filterItems' is set
		if (filters == null) {
			throw new ApiException(400, "Missing the required parameter 'filterItems' when calling queryProperties");
		}

		// create path and map variables
		String localVarPath = "/cloudSafe/queryCloudSafe";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxResults", maxResults));

		GenericType<List<AsApiCloudSafeFile>> localVarReturnType = new GenericType<List<AsApiCloudSafeFile>>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * add a User
	 *
	 * @param user
	 *            Message to user - device (required)
	 * @return
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public String addUser(AsApiUser user) throws DcemApiException, ApiException {
		// verify the required parameter 'user' is set
		if (user == null) {
			throw new ApiException(400, "Missing the required parameter 'user' when calling addUser");
		}
		// create path and map variables
		String localVarPath = "/user/addUser".replaceAll("\\{format\\}", "json");
		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();
		GenericType<String> localVarReturnType = new GenericType<String>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, user, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Delete User. This is only possible if user has no devices yet.
	 *
	 * @param loginId
	 *            (required)
	 * @throws ApiException
	 *             if fails to make API call
	 */
	public void deleteUser(String loginId) throws DcemApiException, ApiException {

		// verify the required parameter 'loginId' is set
		if (loginId == null) {
			throw new ApiException(400, "Missing the required parameter 'loginId' when calling deleteUser");
		}
		// create path and map variables
		String localVarPath = "/user/deleteUser".replaceAll("\\{format\\}", "json");

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "loginId", loginId));
		apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, null, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType,
				localVarAuthNames, null);
	}

	/**
	 *
	 * Verify User, Password and Passcode
	 * @param userLoginId  (required)
	 * @param password  (optional)
	 * @param passcode  (optional)
	 * @throws ApiException if fails to make API call
	 */
	// public void verifyUser(String userLoginId, String password, String passcode) throws AsApiException, ApiException
	// {
	// Object localVarPostBody = null;
	//
	// // verify the required parameter 'userLoginId' is set
	// if (userLoginId == null) {
	// throw new ApiException(400, "Missing the required parameter 'userLoginId' when calling verifyUser");
	// }
	//
	// // create path and map variables
	// String localVarPath = "/user/verifyUser".replaceAll("\\{format\\}","json");
	//
	// // query params
	// List<Pair> localVarQueryParams = new ArrayList<Pair>();
	// Map<String, String> localVarHeaderParams = new HashMap<String, String>();
	// Map<String, Object> localVarFormParams = new HashMap<String, Object>();
	//
	// localVarQueryParams.addAll(apiClient.parameterToPairs("", "userLoginId", userLoginId));
	// localVarQueryParams.addAll(apiClient.parameterToPairs("", "password", password));
	// localVarQueryParams.addAll(apiClient.parameterToPairs("", "passcode", passcode));
	//
	// apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams,
	// localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, null);
	// }

	/**
	 *
	 * Get a List of Authentication Methods defined in the policies
	 * @param userLoginId this is the user Login ID (required)
	 * @param authMethod  (optional)
	 * @param password if password is null, the password will not be verified. This is the UTF-8 Format (optional)
	 * @param passcode if passcode is null, the password will not be verified (optional)
	 * @param networkAddress This is the network Address of source. This will be used to select the policy (optional)
	 * @param fingerPrint This is a fingerptint of the sources. Used for policy (optional)
	 * @return AsApiAuthenticateResponse
	 * @throws ApiException if fails to make API call
	 */
	public AsApiAuthenticateResponse authenticate(String userLoginId, String authMethod, String password, String passcode, String networkAddress,
			String fingerPrint, boolean ignorePassword, String fidoResponse, String rpId) throws DcemApiException, ApiException {
		Object localVarPostBody = null;

		try {
			userLoginId = userLoginId != null ? URLEncoder.encode(userLoginId, "UTF-8") : null;
			authMethod = authMethod != null ? URLEncoder.encode(authMethod, "UTF-8") : null;
			password = password != null ? URLEncoder.encode(password, "UTF-8") : null;
			passcode = passcode != null ? URLEncoder.encode(passcode, "UTF-8") : null;
			networkAddress = networkAddress != null ? URLEncoder.encode(networkAddress, "UTF-8") : null;
			fingerPrint = fingerPrint != null ? URLEncoder.encode(fingerPrint, "UTF-8") : null;
			fidoResponse = fidoResponse != null ? URLEncoder.encode(fidoResponse, "UTF-8") : null;
			rpId = rpId != null ? URLEncoder.encode(rpId, "UTF-8") : null;
		} catch (UnsupportedEncodingException e) {
			throw new ApiException(400, "A parameter could not be encoded to UTF-8");
		}

		// verify the required parameter 'userLoginId' is set
		if (userLoginId == null) {
			throw new ApiException(400, "Missing the required parameter 'userLoginId' when calling authenticate");
		}

		// create path and map variables
		String localVarPath = "/login/authenticate";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<>();
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "userLoginId", userLoginId));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "authMethod", authMethod));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "password", password));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "passcode", passcode));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "networkAddress", networkAddress));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "fingerPrint", fingerPrint));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "ignorePassword", ignorePassword));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "fidoResponse", fidoResponse));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "rpId", rpId));

		Map<String, String> localVarHeaderParams = new HashMap<>();
		Map<String, Object> localVarFormParams = new HashMap<>();

		GenericType<AsApiAuthenticateResponse> localVarReturnType = new GenericType<AsApiAuthenticateResponse>() {
		};
		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * This method retrieves the authentication-methods available for this application. This is used for a pre-selection of authentication-methods for the user. The authentication-methods are configured  in the policy assigned to this application or this application-type.
	 * @throws ApiException if fails to make API call
	 */
	public List<AsApiAuthMethod> getAuthenticateMethods() throws DcemApiException, ApiException {
		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/login/authenticateMethods";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<>();
		Map<String, String> localVarHeaderParams = new HashMap<>();
		Map<String, Object> localVarFormParams = new HashMap<>();

		GenericType<List<AsApiAuthMethod>> localVarReturnType = new GenericType<List<AsApiAuthMethod>>() {
		};
		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	   * 
	   * Get the GUI Configuration (visible views, title, etc.) of the UserPortal as defined by the UserPortal Module.
	   * @return AsApiUPGuiConfig
	   * @throws ApiException if fails to make API call
	 * @throws AsApiException 
	   */
//	public AsApiUPGuiConfig getUserPortalGuiConfig() throws ApiException, AsApiException {
//		Object localVarPostBody = null;
//
//		// create path and map variables
//		String localVarPath = "/userPortal/getGuiConfig";
//
//		// query params
//		List<Pair> localVarQueryParams = new ArrayList<Pair>();
//		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
//		Map<String, Object> localVarFormParams = new HashMap<String, Object>();
//
//		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
//
//		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
//
//		GenericType<AsApiUPGuiConfig> localVarReturnType = new GenericType<AsApiUPGuiConfig>() {
//		};
//		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
//				localVarContentType, localVarAuthNames, localVarReturnType);
//	}

	/**
	 *
	 * Returns all active activation codes from a user
	 * @param filterItems Get all activation-code records which full fills the filter Following filter item names exists user.loginId, createdOn , validTill, info (optional)
	 * @param offset  (optional)
	 * @param maxResults  (optional)
	 * @return List&lt;AsApiActivationCode&gt;
	 * @throws ApiException if fails to make API call
	 */
	public List<AsApiActivationCode> queryActivationCodes(List<AsApiFilterItem> filterItems, int offset, int maxResults)
			throws DcemApiException, ApiException {
		Object localVarPostBody = filterItems;

		// create path and map variables
		String localVarPath = "/user/queryActivationCodes";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxResults", maxResults));

		GenericType<List<AsApiActivationCode>> localVarReturnType = new GenericType<List<AsApiActivationCode>>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	public List<AsApiDevice> queryDevices(List<AsApiFilterItem> filterItems, int offset, int maxResults) throws DcemApiException, ApiException {
		Object localVarPostBody = filterItems;

		// verify the required parameter 'filterItems' is set
		if (filterItems == null) {
			throw new ApiException(400, "Missing the required parameter 'filterItems' when calling queryDevices");
		}

		// create path and map variables
		String localVarPath = "/device/queryDevices";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<>();
		Map<String, String> localVarHeaderParams = new HashMap<>();
		Map<String, Object> localVarFormParams = new HashMap<>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxResults", maxResults));

		// final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		GenericType<List<AsApiDevice>> localVarReturnType = new GenericType<List<AsApiDevice>>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Returns all FIDO Authenticators from a user
	 * @param filterItems Get all FIDO Authenticator records which fullfill the following filters user.loginId, registeredOn, lastUsed (optional)
	 * @param offset  (optional)
	 * @param maxResults  (optional)
	 * @return List&lt;AsApiFidoAuthenticator&gt;
	 * @throws ApiException if fails to make API call
	 * @throws DcemApiException
	 */
	public List<AsApiFidoAuthenticator> queryFidoAuthenticators(List<AsApiFilterItem> filterItems, int offset, int maxResults)
			throws ApiException, DcemApiException {
		Object localVarPostBody = filterItems;

		// create path and map variables
		String localVarPath = "/device/queryFidoAuthenticators";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxResults", maxResults));

		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		GenericType<List<AsApiFidoAuthenticator>> localVarReturnType = new GenericType<List<AsApiFidoAuthenticator>>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Set the state of a device
	 * @param deviceId  (optional)
	 * @param enableState  (optional)
	 * @throws ApiException if fails to make API call
	 */
	public void setDeviceState(int deviceId, boolean enableState) throws ApiException, DcemApiException {
		Object localVarPostBody = null;

		if (deviceId == 0) {
			throw new ApiException(400, "Missing the required parameter 'deviceId' when calling deleteDevice");
		}

		// create path and map variables
		String localVarPath = "/device/setDeviceState";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "enableState", enableState));

		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

	/**
	 *
	 * deletes a device
	 * @param deviceId  (optional)
	 * @throws ApiException if fails to make API call
	 */
	public void deleteDevice(int deviceId) throws ApiException, DcemApiException {
		Object localVarPostBody = null;

		// create path and map variables
		String localVarPath = "/device/deleteDevice";

		if (deviceId == 0) {
			throw new ApiException(400, "Missing the required parameter 'deviceId' when calling deleteDevice");
		}

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "deviceId", deviceId));

		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

	/**
	 *
	 * deletes an activationCode
	 * @param activationCodeId  (required)
	 * @throws ApiException if fails to make API call
	 */
	public void deleteActivationCode(int activationCodeId) throws ApiException, DcemApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'activationCodeId' is set
		if (activationCodeId == 0) {
			throw new ApiException(400, "Missing the required parameter 'activationCodeId' when calling deleteActivationCode");
		}

		// create path and map variables
		String localVarPath = "/user/deleteActivationCode";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "activationCodeId", activationCodeId));

		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

	/**
	 *
	 * Queries
	 * @param filterItems Get all Otp-Tokens which full fills the filter (required)
	 * @param offset  (optional)
	 * @param maxResults  (optional)
	 * @return List&lt;AsApiOtpToken&gt;
	 * @throws ApiException if fails to make API call
	 */
	public List<AsApiOtpToken> queryOtpTokens(List<AsApiFilterItem> filterItems, int offset, int maxResults) throws ApiException, DcemApiException {
		Object localVarPostBody = filterItems;

		// verify the required parameter 'filterItems' is set
		if (filterItems == null) {
			throw new ApiException(400, "Missing the required parameter 'filterItems' when calling queryOtpTokens");
		}

		// create path and map variables
		String localVarPath = "/token/queryOtpTokens";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "offset", offset));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "maxResults", maxResults));

		GenericType<List<AsApiOtpToken>> localVarReturnType = new GenericType<List<AsApiOtpToken>>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * Modify Token. Note the serial number cannot be modified and must match to the token id
	 * @param token The new token object. The ID and serial number must match. (required)
	 * @param passcode The Passcode which is shown in the token (optional)
	 * @throws ApiException if fails to make API call
	 */
	public void modifyOtpToken(AsApiOtpToken token, String passcode) throws ApiException, DcemApiException {
		Object localVarPostBody = token;

		// verify the required parameter 'token' is set
		if (token == null) {
			throw new ApiException(400, "Missing the required parameter 'token' when calling modifyOtpToken");
		}

		// create path and map variables
		String localVarPath = "/token/modifyOtpToken";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<>(1);
		Map<String, String> localVarHeaderParams = new HashMap<>();
		Map<String, Object> localVarFormParams = new HashMap<>();

		if (passcode != null) {
			localVarQueryParams.addAll(apiClient.parameterToPairs("", "passcode", passcode));
		}
		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

	/**
	 *
	 * Modify Token. Note the serial number cannot be modified and must match to the token id
	 * @param token The new token object. The ID and serial number must match. (required)
	 * @throws ApiException if fails to make API call
	 */
	public void modifyOtpToken(AsApiOtpToken token) throws ApiException, DcemApiException {
		Object localVarPostBody = token;

		// verify the required parameter 'token' is set
		if (token == null) {
			throw new ApiException(400, "Missing the required parameter 'token' when calling modifyToken");
		}

		// create path and map variables
		String localVarPath = "/token/modifyOtpToken";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

	/**
	 *
	 * Request a FIDO create JSON
	 * @param username the loginId of the user who will register a device (required)
	 * @throws ApiException if fails to make API call
	 * @throws DcemApiException
	 */
	public String fidoStartRegistration(String username, String rpId) throws ApiException, DcemApiException {

		try {
			username = URLEncoder.encode(username, "UTF-8");
			rpId = URLEncoder.encode(rpId, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ApiException(400, "The required parameters 'username' and 'rpId' are invalid when calling fidoStartRegistration");
		}

		Object localVarPostBody = null;

		// verify the required parameter 'username' is set
		if (username == null) {
			throw new ApiException(400, "Missing the required parameter 'username' when calling fidoStartRegistration");
		}

		// verify the required parameter 'rpId' is set
		if (rpId == null) {
			throw new ApiException(400, "Missing the required parameter 'rpId' when calling fidoStartRegistration");
		}

		// create path and map variables
		String localVarPath = "/device/fidoStartRegistration";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "username", username));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "rpId", rpId));

		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		GenericType<String> localVarReturnType = new GenericType<String>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	   * 
	   * send a FIDO create response JSON for validation
	   * @param responseJson a FIDO create response in JSON format (required)
	   * @param displayName a Display Name for the new FIDO key (required)
	   * @throws ApiException if fails to make API call
	 * @throws DcemApiException 
	   */
	public String fidoFinishRegistration(String responseJson, String displayName) throws ApiException, DcemApiException {

		try {
			responseJson = URLEncoder.encode(responseJson, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ApiException(400, "The required parameter 'responseJson' is invalid when calling fidoFinishRegistration");
		}

		Object localVarPostBody = null;

		// verify the required parameter 'responseJson' is set
		if (responseJson == null) {
			throw new ApiException(400, "Missing the required parameter 'responseJson' when calling fidoFinishRegistration");
		}

		// verify the required parameter 'displayName' is set
		if (displayName == null) {
			throw new ApiException(400, "Missing the required parameter 'displayName' when calling fidoFinishRegistration");
		}

		// create path and map variables
		String localVarPath = "/device/fidoFinishRegistration";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "responseJson", responseJson));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "displayName", displayName));

		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		GenericType<String> localVarReturnType = new GenericType<String>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/**
	 *
	 * deletes a FIDO Authenticator
	 * @param fidoAuthenticatorId  (required)
	 * @throws ApiException if fails to make API call
	 * @throws DcemApiException
	 */
	public void deleteFidoAuthenticator(int fidoAuthenticatorId) throws ApiException, DcemApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'fidoAuthenticatorId' is set
		if (fidoAuthenticatorId == 0) {
			throw new ApiException(400, "Missing the required parameter 'fidoAuthenticatorId' when calling deleteFidoAuthenticator");
		}

		// create path and map variables
		String localVarPath = "/device/deleteFidoAuthenticator";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "fidoAuthenticatorId", fidoAuthenticatorId));
		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

	/**
	 *
	 * change the user password
	 * @param userLoginId  (required)
	 * @param oldPassword  (required)
	 * @param newPassword  (required)
	 * @throws ApiException if fails to make API call
	 */
	public void changePassword(String userLoginId, String oldPassword, String newPassword) throws ApiException, DcemApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'userLoginId' is set
		if (userLoginId == null) {
			throw new ApiException(400, "Missing the required parameter 'userLoginId' when calling changePassword");
		}

		// verify the required parameter 'oldPassword' is set
		if (oldPassword == null) {
			throw new ApiException(400, "Missing the required parameter 'oldPassword' when calling changePassword");
		}

		// verify the required parameter 'newPassword' is set
		if (newPassword == null) {
			throw new ApiException(400, "Missing the required parameter 'newPassword' when calling changePassword");
		}

		// create path and map variables
		String localVarPath = "/user/changePassword";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "userLoginId", userLoginId));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "oldPassword", oldPassword));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "newPassword", newPassword));

		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

//	public List<AsApiShareCloudSafeDetails> getShareCloudSafeFiles(String userLoginId, String nameFilter) throws ApiException, AsApiException {
//
//		Object localVarPostBody = null;
//		if (userLoginId == null) {
//			throw new ApiException(400, "Missing the required parameter 'userLoginId' when calling getShareCloudSafeFiles");
//		}
//
//		// create path and map variables
//		String localVarPath = "/cloudSafe/getSharedCloudSafeFiles";
//
//		// query params
//		Map<String, Object> localVarFormParams = new HashMap<>();
//		List<Pair> localVarQueryParams = new ArrayList<>();
//		localVarQueryParams.addAll(apiClient.parameterToPairs("", "userLoginId", userLoginId));
//		localVarQueryParams.addAll(apiClient.parameterToPairs("", "nameFilter", nameFilter));
//		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
//
//		GenericType<List<AsApiShareCloudSafeDetails>> localVarReturnType = new GenericType<List<AsApiShareCloudSafeDetails>>() {
//		};
//		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
//				localVarContentType, localVarAuthNames, localVarReturnType);
//	}

	

//	public void shareCloudSafeFile(AsApiShareCloudSafe shareCloudSafe) throws ApiException, AsApiException {
//		if (shareCloudSafe == null) {
//			throw new ApiException(400, "Missing the required parameter 'cloudSafeFile' when calling shareCloudSafeFile");
//		}
//		if (shareCloudSafe.getUserLoginId() == null) {
//			throw new ApiException(400, "Missing the required parameter 'userLoginId' when calling shareCloudSafeFile");
//		}
//		Object localVarPostBody = shareCloudSafe;
//		// create path and map variables
//		String localVarPath = "/cloudSafe/shareCloudSafeFile";
//
//		// query params
//		List<Pair> localVarQueryParams = new ArrayList<Pair>();
//		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
//		Map<String, Object> localVarFormParams = new HashMap<String, Object>();
//
//		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
//				localVarContentType, localVarAuthNames, null);
//
//	}

	/**
	 *
	 * verifies the url token
	 * @param urlToken This is the Url Token object. (required)
	 * @return AsApiUser
	 * @throws ApiException if fails to make API call
	 */
	public AsApiUser verifyUrlToken(AsApiUrlToken urlToken) throws ApiException, DcemApiException {
		Object localVarPostBody = urlToken;

		// verify the required parameter 'urlToken' is set
		if (urlToken == null) {
			throw new ApiException(400, "Missing the required parameter 'urlToken' when calling verifyUrlToken");
		}

		// create path and map variables
		String localVarPath = "/user/verifyUrlToken";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
		GenericType<AsApiUser> localVarReturnType = new GenericType<AsApiUser>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}

	/*public AsApiUser verifyUrlToken(String urlToken) throws ApiException, AsApiException {
		Object localVarPostBody = null;
	
		// create path and map variables
		String localVarPath = "/user/verifyUrlToken";
	
		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();
	
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "urlToken", urlToken));
	
		//	final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
	
		//	final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);
	
		GenericType<AsApiUser> localVarReturnType = new GenericType<AsApiUser>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}*/

	/**
	 *
	 * request an url token for password reset
	 * @param urlToken This is the Url Token object. (required)
	 * @throws ApiException if fails to make API call
	 */
	public void addUrlToken(AsApiUrlToken urlToken) throws ApiException, DcemApiException {
		Object localVarPostBody = urlToken;

		// verify the required parameter 'urlToken' is set
		if (urlToken == null) {
			throw new ApiException(400, "Missing the required parameter 'urlToken' when calling addUrlToken");
		}

		// create path and map variables
		String localVarPath = "/user/addUrlToken";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

	/**
	 *
	 * sets the user password after a password reset
	 * @param userLoginId  (required)
	 * @param newPassword  (required)
	 * @throws ApiException if fails to make API call
	 */
	public void setPassword(String userLoginId, String newPassword) throws ApiException, DcemApiException {
		Object localVarPostBody = null;

		// verify the required parameter 'userLoginId' is set
		if (userLoginId == null) {
			throw new ApiException(400, "Missing the required parameter 'userLoginId' when calling setPassword");
		}

		// verify the required parameter 'newPassword' is set
		if (newPassword == null) {
			throw new ApiException(400, "Missing the required parameter 'newPassword' when calling setPassword");
		}

		// create path and map variables
		String localVarPath = "/user/setPassword";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		localVarQueryParams.addAll(apiClient.parameterToPairs("", "userLoginId", userLoginId));
		localVarQueryParams.addAll(apiClient.parameterToPairs("", "newPassword", newPassword));

		final String localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);

		final String localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

		apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, null);
	}

	/**
	   * 
	   * The List of users who have access to this file.
	   * @param cloudSafeFile This is the cloudSafe object. (required)
	   * @return ApiResponse&lt;List&lt;AsApiShareCloudSafeAccess&gt;&gt;
	   * @throws ApiException if fails to make API call
	   */
	public List<AsApiShareCloudSafe> getSharedCloudSafeUsersAccess(AsApiCloudSafeFile cloudSafeFile) throws ApiException, DcemApiException {
		Object localVarPostBody = cloudSafeFile;

		// verify the required parameter 'cloudSafeFile' is set
		if (cloudSafeFile == null) {
			throw new ApiException(400, "Missing the required parameter 'cloudSafeFile' when calling getSharedCloudSafeUsersAccess");
		}

		// create path and map variables
		String localVarPath = "/cloudSafe/getSharedCloudSafeUsersAccess";

		// query params
		List<Pair> localVarQueryParams = new ArrayList<Pair>();
		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
		Map<String, Object> localVarFormParams = new HashMap<String, Object>();

		GenericType<List<AsApiShareCloudSafe>> localVarReturnType = new GenericType<List<AsApiShareCloudSafe>>() {
		};
		return apiClient.invokeAPI(localVarPath, "POST", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
				localVarContentType, localVarAuthNames, localVarReturnType);
	}
	
	/**
	   * 
	   * Verifies the Registration for User 
	   * @param userLoginId This is the full qualified user login ID including the domain name seperated ba a back slash (optional)
	   * @return ApiResponse&lt;String&gt;
	   * @throws ApiException if fails to make API call
	   */
//	  public String verifyRegistrationUser (String userLoginId) throws ApiException, AsApiException {
//		Object localVarPostBody = null;
//
//		// verify the required parameter 'sessionId' is set
//		if (userLoginId == null) {
//		      throw new ApiException(400, "Missing the required parameter 'userLoginId' when calling verifyRegistration");
//		}
//
//		// create path and map variables
//		String localVarPath = "/userPortal/verifyRegistrationUser";
//
//		// query params
//		List<Pair> localVarQueryParams = new ArrayList<Pair>();
//		localVarQueryParams.addAll(apiClient.parameterToPairs("", "userLoginId", userLoginId));
//		Map<String, String> localVarHeaderParams = new HashMap<String, String>();
//		Map<String, Object> localVarFormParams = new HashMap<String, Object>();
//
//		GenericType<String> localVarReturnType = new GenericType<String>() {
//		};
//		return apiClient.invokeAPI(localVarPath, "GET", localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept,
//				localVarContentType, localVarAuthNames, localVarReturnType);
//	}

}
