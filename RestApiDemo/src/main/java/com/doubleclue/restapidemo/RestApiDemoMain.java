package com.doubleclue.restapidemo;

/*
 * This is a simple REST Web-Services client Demo
 * This projects needs the library "LibRestDcClient.jar" 
 * 
 * This demo send a message to a user with the as.Login template and waits for the message response 
 * 
 * 
 * 
 * 
 * Maven Dependencies: 
 *  	<dependency>
			<groupId>com.doubleclue.lib</groupId>
			<artifactId>LibRestDcClient</artifactId>
		</dependency>
 * 
 * 
 */

import java.util.ArrayList;
import java.util.List;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.RestConnectionConfig;
import com.doubleclue.as.restapi.model.AddMessageResponse;
import com.doubleclue.as.restapi.model.AsApiMessage;
import com.doubleclue.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.as.restapi.model.AsMapEntry;

public class RestApiDemoMain {
	
	final static int WAIT_FOR_RESPONSE_SECONDS = 5;

	public static void main(String[] args) {
		
		RestConnectionConfig restConnectionConfig = new RestConnectionConfig();
		restConnectionConfig.setRestApiUrl("http://hws001S0210:8001/dcem/restApi/as");
		restConnectionConfig.setPassword("password");
		AsClientRestApi.initilize(restConnectionConfig);

		AsClientRestApi clientRestApi = AsClientRestApi.getInstance();

		List<AsMapEntry> dataMap = new ArrayList<AsMapEntry>();
		AsMapEntry code = new AsMapEntry();
		code.setKey("code");
		code.setValue("4711");
		dataMap.add(code);

		AsApiMessage apiMessage = new AsApiMessage("emanuel.galea", "as.Login", dataMap, true);

		AddMessageResponse addMessageResponse = null;
		try {
			addMessageResponse = clientRestApi.addMessage(apiMessage);
		} catch (Exception e) {
			System.err.println("Exception when calling DefaultApi#addMessage");
			System.err.println(e.toString());
			e.printStackTrace();
			System.exit(1);
		}

		// wait for User Message Response
		int iterations = addMessageResponse.getTimeToLive() / WAIT_FOR_RESPONSE_SECONDS;
		for (int i = 0; i < iterations; i++) {
			try {
				AsApiMessageResponse messageResponse = clientRestApi.getMessageResponse(addMessageResponse.getMsgId(), WAIT_FOR_RESPONSE_SECONDS);
				if (messageResponse.getFinal()) {
					// message received
					if (messageResponse.getMsgStatus().equals(AsApiMsgStatus.OK)) {
						if (messageResponse.getActionId().equals("ok")) {
							System.out.println("Message OK");
						} else {
							System.out.println ("Message denied");
						}
					} else {
						System.err.println("Message Response ERROR: " + messageResponse.getMsgStatus());
					}
					break;
				}
				System.out.println("Message status: " + messageResponse.getMsgStatus() + ", time left in seconds: " + (addMessageResponse.getTimeToLive()- (i * WAIT_FOR_RESPONSE_SECONDS)));
			} catch (Exception e) {
				System.err.println("Exception when calling DefaultApi#addMessage");
				System.err.println(e.toString());
				e.printStackTrace();
				break;
			}
		}
		System.out.println("Ready");
		System.exit(0);

	}

}
