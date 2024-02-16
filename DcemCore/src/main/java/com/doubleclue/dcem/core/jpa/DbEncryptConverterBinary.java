package com.doubleclue.dcem.core.jpa;

import java.io.Serializable;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.exceptions.DcemException;



@SuppressWarnings("serial")
@Converter
public class DbEncryptConverterBinary implements AttributeConverter< byte[], byte[]>, Serializable {
	
	private static final Logger logger = LogManager.getLogger(DbEncryptConverterBinary.class);


	@Override
	public byte[] convertToDatabaseColumn(byte [] attribute) {
		if (attribute == null) {
			return null;
		}
		try {
			return DbEncryption.encryptSeed(attribute);
		} catch (DcemException exp) {
			logger.error("Couldn't encryrt Value", exp);
			throw new RuntimeException(exp);
		}
	}

	@Override
	public byte[]  convertToEntityAttribute(byte[] dbData) {
		if (dbData == null || dbData.length == 0) {
			return null;
		}
		try {
			return DbEncryption.decryptSeed(dbData);
		} catch (DcemException exp) {
			logger.error("Couldn't decrpt Value", exp);
			return null;
//			throw new RuntimeException(exp);
		}
	}



	


}
