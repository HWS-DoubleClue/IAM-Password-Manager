package com.doubleclue.dcem.core.jpa;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.AttributeConverter;


//@Converter
public class EpochDateConverter implements AttributeConverter<Date, Integer> {

	@Override
	public Integer convertToDatabaseColumn(Date date) {
		if (date == null) {
			return 0;
		}
		return (int) (date.getTime() / 1000);
		
	}

	@Override
	public Date convertToEntityAttribute(Integer dateInt) {
		if (dateInt == null) {
			return null;
		}
		long dateLong = dateInt;
		return new Timestamp (dateLong * 1000);
	}



	


}
