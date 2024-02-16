package com.doubleclue.dcem.core.jpa;

import java.io.Serializable;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.doubleclue.dcem.core.exceptions.DcemException;


@SuppressWarnings("serial")
@Converter
public class DbEncryptConverter implements AttributeConverter<String, byte[]>, Serializable {

	@Override
	public byte[] convertToDatabaseColumn(String attribute) {
		if (attribute == null) {
			return null;
		}
		try {
			return DbEncryption.encryptSeed(attribute);
		} catch (DcemException exp) {
			throw new RuntimeException(exp);
		}
	}

	@Override
	public String convertToEntityAttribute(byte[] dbData) {
		if (dbData == null) {
			return null;
		}
		try {
			return DbEncryption.decryptSeedToString(dbData);
		} catch (DcemException exp) {
			throw new RuntimeException(exp);
		}
	}



	


}
