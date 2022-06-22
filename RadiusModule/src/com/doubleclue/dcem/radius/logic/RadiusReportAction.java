package com.doubleclue.dcem.radius.logic;

public enum RadiusReportAction {
	createChallenge, SharedSecret, DecodingException, HandlingException, InvalidChallengState, RetrieveMsg, InvalidStateUser, SendError, 
	NoPasswordReceived, OK, Rejected, MULTI_AUTH_METHODS, SMS_WITHOUT_CHALLENG, NoUserAttribute,InvalidCharacterEncoding ;
}
