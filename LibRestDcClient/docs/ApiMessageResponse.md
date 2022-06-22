
# ApiMessageResponse

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** |  | 
**_final** | **Boolean** |  | 
**msgStatus** | [**MsgStatusEnum**](#MsgStatusEnum) |  | 
**inputMap** | [**List&lt;MapEntry&gt;**](MapEntry.md) |  |  [optional]
**info** | **String** |  |  [optional]
**signature** | **byte[]** |  |  [optional]


<a name="MsgStatusEnum"></a>
## Enum: MsgStatusEnum
Name | Value
---- | -----
OK | &quot;Ok&quot;
WAITING | &quot;Waiting&quot;
QUEUED | &quot;Queued&quot;
SENDING | &quot;Sending&quot;
RECERROR | &quot;RecError&quot;
SENDERROR | &quot;SendError&quot;
DISCONNECTED | &quot;Disconnected&quot;



