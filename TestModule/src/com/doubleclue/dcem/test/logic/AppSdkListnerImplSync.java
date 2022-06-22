package com.doubleclue.dcem.test.logic;

import com.doubleclue.comm.thrift.AuthAppMessageResponse;
import com.doubleclue.comm.thrift.AuthUserResponse;
import com.doubleclue.sdk.api.AppSdkListenerSync;
import com.doubleclue.sdk.api.AsErrorCodes;
import com.doubleclue.sdk.api.AsException;
import com.doubleclue.sdk.api.ReceivedMessage;
import com.doubleclue.sdk.api.SdkListenerMethods;

public class AppSdkListnerImplSync implements AppSdkListenerSync {

	public Object synchObject = new Object();
	ReceivedMessage receivedMessage;
	// AppStatus appStatus;
	// AsException asException;
	AuthAppMessageResponse authAppMessageResponse;
	AuthUserResponse authUserResponse;
	private SdkListenerMethods methodReceived;

	@Override
	public void onReceiveMessage(ReceivedMessage receivedMessage) {
		this.receivedMessage = receivedMessage;
		trigger(SdkListenerMethods.onReceiveMessage);
	}

	@Override
	public void onDisconnect(String reason) {
		trigger(SdkListenerMethods.onDisconnect);
	}
	
	public void resetMethod() {
		this.methodReceived = null;
	}

	void trigger(SdkListenerMethods onMessageRec) {
		System.out.println("AppSdkListnerImpl.trigger() RECEIVED: " + onMessageRec.name());
		this.methodReceived = onMessageRec;
		synchronized (synchObject) {
			synchObject.notify();
		}
	}

	public SdkListenerMethods waitFor(SdkListenerMethods sdkListnerMethods, int time) throws AsException {

		synchronized (synchObject) {
			if (methodReceived == null) {
				try {
					synchObject.wait(time);
				} catch (InterruptedException e) {
					throw new AsException(AsErrorCodes.GENERIC, e.toString());
				}
			}
			if (methodReceived == null) {
				throw new AsException(AsErrorCodes.RESPONSE_TIMED_OUT, sdkListnerMethods.name());
			}
			if (sdkListnerMethods != null && sdkListnerMethods != methodReceived) {
				String recevied = methodReceived.name();
				methodReceived = null;
				throw new AsException(AsErrorCodes.UNEXPECTED_RESPONSE_RECEIVED, "waiting for: " +sdkListnerMethods.name() + ", instead we got: " + recevied);
			}
			return methodReceived;
		}
	}

	public ReceivedMessage getReceivedMessage() {
		return receivedMessage;
	}

	public void setReceivedMessage(ReceivedMessage receivedMessage) {
		this.receivedMessage = receivedMessage;
	}

	public AuthAppMessageResponse getAuthAppMessageResponse() {
		return authAppMessageResponse;
	}

	public void setAuthAppMessageResponse(AuthAppMessageResponse authAppMessageResponse) {
		this.authAppMessageResponse = authAppMessageResponse;
	}

	@Override
	public void onAuthenticateUserMessageResponse(AuthAppMessageResponse authAppMessageResponse) {
		this.authAppMessageResponse = authAppMessageResponse;
		trigger(SdkListenerMethods.onAuthenticateUserMessageResponse);
	}

	@Override
	public void onLogoff(String reason) {
		trigger(SdkListenerMethods.onLogoff);
	}

	
}
