# DefaultApi

All URIs are relative to *http://localhost/restApi*

Method | HTTP request | Description
------------- | ------------- | -------------
[**addMessage**](DefaultApi.md#addMessage) | **POST** /message/add | 
[**cancelMessage**](DefaultApi.md#cancelMessage) | **POST** /message/cancel | 
[**cancelUserMessages**](DefaultApi.md#cancelUserMessages) | **POST** /message/cancelUserMessages | 
[**echo**](DefaultApi.md#echo) | **GET** /misc/echo | 
[**getMessageResponse**](DefaultApi.md#getMessageResponse) | **GET** /message | 
[**queryLoginCode**](DefaultApi.md#queryLoginCode) | **GET** /loginQrCode/queryLoginQrCode | 
[**requestLoginQrCode**](DefaultApi.md#requestLoginQrCode) | **GET** /loginQrCode/requestLoginQrCode | 


<a name="addMessage"></a>
# **addMessage**
> AddMessageResponse addMessage(apiMessage)



Creates a new message to user

### Example
```java
// Import classes:
//import com.doubleclue.as.restapi.ApiException;
//import com.doubleclue.as.restapi.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
AsApiMessage apiMessage = new AsApiMessage(); // AsApiMessage | Message to user - device
try {
    AddMessageResponse result = apiInstance.addMessage(apiMessage);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#addMessage");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **apiMessage** | [**AsApiMessage**](AsApiMessage.md)| Message to user - device |

### Return type

[**AddMessageResponse**](AddMessageResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="cancelMessage"></a>
# **cancelMessage**
> cancelMessage(msgId)



Cancel a pending mesage

### Example
```java
// Import classes:
//import eu.kara.as.restapi.ApiException;
//import eu.kara.as.restapi.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
Long msgId = 789L; // Long | message unique id
try {
    apiInstance.cancelMessage(msgId);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#cancelMessage");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **msgId** | **Long**| message unique id |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="cancelUserMessages"></a>
# **cancelUserMessages**
> cancelUserMessages(msgId)



Cancal all user pending messages

### Example
```java
// Import classes:
//import eu.kara.as.restapi.ApiException;
//import eu.kara.as.restapi.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
String msgId = "msgId_example"; // String | user name
try {
    apiInstance.cancelUserMessages(msgId);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#cancelUserMessages");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **msgId** | **String**| user name |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="echo"></a>
# **echo**
> String echo(text)



This is used for testing purposes

### Example
```java
// Import classes:
//import eu.kara.as.restapi.ApiException;
//import eu.kara.as.restapi.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
String text = "text_example"; // String | 
try {
    String result = apiInstance.echo(text);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#echo");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **text** | **String**|  | [optional]

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json, text/xml, text/html

<a name="getMessageResponse"></a>
# **getMessageResponse**
> AsApiMessageResponse getMessageResponse(msgId)



Returns all pets from the system that the user has access to

### Example
```java
// Import classes:
//import eu.kara.as.restapi.ApiException;
//import eu.kara.as.restapi.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
Long msgId = 789L; // Long | The message Id returned by post Message
try {
    AsApiMessageResponse result = apiInstance.getMessageResponse(msgId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#getMessageResponse");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **msgId** | **Long**| The message Id returned by post Message |

### Return type

[**AsApiMessageResponse**](AsApiMessageResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="queryLoginCode"></a>
# **queryLoginCode**
> QueryLoginQrCodeResponse queryLoginCode(sessionId)



Queries if the login code was consumed

### Example
```java
// Import classes:
//import eu.kara.as.restapi.ApiException;
//import eu.kara.as.restapi.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
String sessionId = "sessionId_example"; // String | this is the session Id of the portal
try {
    QueryLoginQrCodeResponse result = apiInstance.queryLoginCode(sessionId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#queryLoginCode");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sessionId** | **String**| this is the session Id of the portal |

### Return type

[**QueryLoginQrCodeResponse**](QueryLoginQrCodeResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="requestLoginQrCode"></a>
# **requestLoginQrCode**
> AsApiException requestLoginQrCode(sessionId)



Get a Byte Array for Qr-Code generation

### Example
```java
// Import classes:
//import eu.kara.as.restapi.ApiException;
//import eu.kara.as.restapi.DefaultApi;


DefaultApi apiInstance = new DefaultApi();
String sessionId = "sessionId_example"; // String | this is the session Id of the portal
try {
    AsApiException result = apiInstance.requestLoginQrCode(sessionId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling DefaultApi#requestLoginQrCode");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **sessionId** | **String**| this is the session Id of the portal |

### Return type

[**AsApiException**](AsApiException.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

