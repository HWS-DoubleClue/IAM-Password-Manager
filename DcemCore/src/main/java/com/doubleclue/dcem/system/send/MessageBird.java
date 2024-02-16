package com.doubleclue.dcem.system.send;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.dcem.system.logic.SystemPreferences;
import com.messagebird.MessageBirdClient;
import com.messagebird.MessageBirdService;
import com.messagebird.MessageBirdServiceImpl;
import com.messagebird.exceptions.GeneralException;
import com.messagebird.exceptions.UnauthorizedException;
import com.messagebird.objects.IfMachineType;
import com.messagebird.objects.MessageResponse;
import com.messagebird.objects.VoiceMessage;
import com.messagebird.objects.VoiceMessageResponse;

@ApplicationScoped
public class MessageBird implements SmsApi {
	
	@Inject
	AdminModule adminModule;
	
	@Inject
	SystemModule systemModule;

	static final Logger logger = LogManager.getLogger(MessageBird.class);

	MessageBirdService messageBirdService = null;
	MessageBirdClient messageBirdClient = null;
	String originatorName;

	public void initSms(SystemPreferences preferences) throws DcemException {

		if (preferences.getSmsProviderAccesKey() == null || preferences.getSmsProviderAccesKey().isEmpty()) {
			messageBirdService = null;
			messageBirdClient = null;
			return;
		}
		messageBirdService = new MessageBirdServiceImpl(preferences.getSmsProviderAccesKey());
		if (preferences.getHttpProxyPort() > 0) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(preferences.getHttpProxyHost(), preferences.getHttpProxyPort()));
			((MessageBirdServiceImpl) messageBirdService).setProxy(proxy);
		}
		// Add the service to the client
		messageBirdClient = new MessageBirdClient(messageBirdService);
		originatorName = preferences.getSmsOriginatorName();
	}
	
	public void sendSmsMessage(List<String> telephoneNumbers, String messageBody) throws DcemException {
		sendSmsMessage(telephoneNumbers, messageBody, originatorName);
	}

	/*
	 * (non-Javadoc)
	 *
	 */
	public void sendSmsMessage(List<String> telephoneNumbers, String messageBody, String originator) throws DcemException {

		if (messageBirdClient == null) {
			initSms(systemModule.getPreferences());
			if (messageBirdClient == null) {
				throw new DcemException(DcemErrorCodes.SMS_INVALID_CONFIGURATION, "SMS not initialized");
			}
		}
		MessageResponse messageResponse = null;
		logger.debug("Sending message: " + messageBody + " to " + telephoneNumbers);
		final List<BigInteger> phones = new ArrayList<BigInteger>();
		String defaultCountryCode = adminModule.getPreferences().getDefaultPhoneCountryCode();
		for (final String phoneNumber : telephoneNumbers) {
			phones.add(new BigInteger(prepareTelephoneNumbers(phoneNumber, defaultCountryCode)));
		}
		try {
			messageResponse = messageBirdClient.sendMessage(originator, messageBody, phones);
			if (logger.isDebugEnabled()) {
				logger.debug("SMS message Response: " + messageResponse.toString());
			}
		} catch (UnauthorizedException exp) {
			throw new DcemException(DcemErrorCodes.SMS_UNAUTHORIZED, exp.getMessage(), exp);
		} catch (GeneralException exp) {
			throw new DcemException(DcemErrorCodes.SMS_SEND_EXCEPTION, exp.toString(), exp);
		}
		return;
	}

	/*
	 * (non-Javadoc)
	 *
	 */
	public void sendVoiceMessage(List<String> telephoneNumbers, String messageBody, SupportedLanguage language) throws DcemException {

		if (messageBirdClient == null) {
			initSms(systemModule.getPreferences());
			if (messageBirdClient == null) {
				throw new DcemException(DcemErrorCodes.SMS_INVALID_CONFIGURATION, "VOICE not initialized");
			}
		}
		VoiceMessageResponse messageResponse = null;
		logger.debug("Sending Voice message: " + messageBody + " to " + telephoneNumbers);
		final List<BigInteger> phones = new ArrayList<BigInteger>();
		String defaultCountryCode = adminModule.getPreferences().getDefaultPhoneCountryCode();
		for (final String phoneNumber : telephoneNumbers) {
			phones.add(new BigInteger(prepareTelephoneNumbers(phoneNumber, defaultCountryCode)));
		}
		VoiceMessage voiceMessage = new VoiceMessage(messageBody, phones);
		String lang = null;
		switch (language) {
		case English:
			lang = "en-gb";
			break;
		case German:
			lang = "de-de";
			break;
		case Italian:
			lang = "it-it";
			break;
		case French:
			lang = "fr-fr";
			break;
		}
		if (lang != null) {
			voiceMessage.setLanguage(lang);
		}
		voiceMessage.setRepeat(3);
		voiceMessage.setIfMachine(IfMachineType.cont);
		try {
			messageResponse = messageBirdClient.sendVoiceMessage(voiceMessage);
			if (logger.isDebugEnabled()) {
				logger.debug("Voice message Response: " + messageResponse.toString());
			}

		} catch (UnauthorizedException exp) {
			throw new DcemException(DcemErrorCodes.SMS_UNAUTHORIZED, exp.getMessage(), exp);
		} catch (GeneralException exp) {
			if (exp.getErrors() != null && exp.getErrors().isEmpty() == false) {
				throw new DcemException(DcemErrorCodes.SEND_VOICE_EXCEPTION, exp.getErrors().toString(), exp);
			}
			throw new DcemException(DcemErrorCodes.SEND_VOICE_EXCEPTION, exp.toString(), exp);
		}
		return;
	}

	static String prepareTelephoneNumbers(String telephoneNumber, String defaultCountryCode) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < telephoneNumber.length(); i++) {
			if (telephoneNumber.charAt(i) >= '0' && telephoneNumber.charAt(i) <= '9') {
				sb.append(telephoneNumber.charAt(i));
			}
		}
		String tel = sb.toString();
		if (tel.startsWith("00") || tel.startsWith("+")) {
			return tel;
		}
		if (tel.startsWith("0") && defaultCountryCode != null && defaultCountryCode.isEmpty() == false) {
			return defaultCountryCode + tel.substring(1);
		}
		return tel;
	}

}
