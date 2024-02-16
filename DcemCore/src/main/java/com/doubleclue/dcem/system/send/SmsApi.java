package com.doubleclue.dcem.system.send;

import java.util.List;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.system.logic.SystemPreferences;

public interface SmsApi {
	
	void initSms(SystemPreferences preferences) throws DcemException;
	void sendSmsMessage (List<String> telephoneNumbers, String body) throws DcemException;
	void sendSmsMessage (List<String> telephoneNumbers, String body, String originator) throws DcemException;

}
