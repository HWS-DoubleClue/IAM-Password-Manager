/**
 * 
 */
package com.doubleclue.dcem.core.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author  Emanuel Galea
 *
 */
public class DateUtils {

	private static final long MILLIS_PER_DAY = 24*60*60*1000;
	private static final long MILLIS_PER_HOUR = 24*60*1000;
	private static int DEFAULT_TIMESTYLE = DateFormat.MEDIUM;
	private static int DEFAULT_DATESTYLE = DateFormat.MEDIUM;	
	
	
	/**
	 * formats the given date with default: TimeZone (ServerTimeZone), default Locale (GERMAN), default dateStyle (DateFormat MEDIUM), default timeStyle (TimeFormat MEDIUM)
	 * @param date
	 * @return - null if no date is given, else the formatted string representation
	 */
	public static String formatDateTime(Date date) {
		return formatDateTime(date, null, null, null, null);
	}
	
	
	/**
	 * 
	 * @param date
	 * @param defTimeZone - default is server timezone, nullable
	 * @param locale - default is GERMAN, nullable
	 * @param dateStyle - a DateFormat style definition (it is currently not checked whether the value is a valid DateFormat), default is MEDIUM, nullable 
	 * @param timeStyle - a TimeFormat style definition (it is currently not checked whether the value is a valid TimeFormat), default is MEDIUM, nullable
	 * @return - null if no date is given, else the formatted string representation
	 */
	public static String formatDateTime(Date date, TimeZone defTimeZone, Locale locale, Integer dateStyle, Integer timeStyle) {
		if(date == null) {
			return null;
		}
		if (defTimeZone == null){
			defTimeZone = TimeZone.getDefault();
		}
		if (locale == null){
			locale = Locale.GERMAN;
		}
		if(dateStyle == null){
			dateStyle = DEFAULT_DATESTYLE;
		}
		if(timeStyle == null){
			timeStyle = DEFAULT_TIMESTYLE;
		}
		
		DateFormat df = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
		df.setTimeZone(defTimeZone);
		return df.format(date);
	}
	
	public static boolean isSameDay(Date date1, Date date2) {

	    // Strip out the time part of each date.
	    long julianDayNumber1 = date1.getTime() / MILLIS_PER_DAY;
	    long julianDayNumber2 = date2.getTime() / MILLIS_PER_DAY;

	    // If they now are equal then it is the same day.
	    return julianDayNumber1 == julianDayNumber2;
	}
	
	public static boolean isSameHour(Date date1, Date date2) {

	    // Strip out the time part of each date.
	    long julianDayNumber1 = date1.getTime() / MILLIS_PER_HOUR;
	    long julianDayNumber2 = date2.getTime() / MILLIS_PER_HOUR;

	    // If they now are equal then it is the same day.
	    return julianDayNumber1 == julianDayNumber2;
	}
	
	/**
	 * simple string to date parser
	 * @param sDate - String to be converted into a date
	 * @param clientTimeZone - the given timezone - if null it uses the server timezone. It can be any timezone, also the SystemSettings ServerTimezone definition
	 * @param locale - the given parse locale - if null it assumes GERMAN
	 * @param dateStyle - the style of the date to be created - default is MEDIUM
	 * @param timeStyle - the style of the time to be created - default is MEDIUM
	 * @return parsed Date or null in case sDate could not be parsed (is null, empty, could not be parsed)
	 */
	public static Date parseStringToDateTime(String sDate, TimeZone clientTimeZone, Locale locale, Integer dateStyle, Integer timeStyle){
		if(sDate == null || sDate.isEmpty()) {
			return null;
		}
		if (clientTimeZone == null){
			// set default timezone of host
			clientTimeZone = TimeZone.getDefault();
		}
		if (locale == null){
			locale = Locale.GERMAN;
		}
		if(dateStyle == null){
			dateStyle = DEFAULT_DATESTYLE;
		}
		if(timeStyle == null){
			timeStyle = DEFAULT_TIMESTYLE;
		}
		
		DateFormat df = DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale);
		df.setTimeZone(clientTimeZone);
		Date retDate = null;
		try {
			retDate = df.parse(sDate);
		} catch (ParseException e) {
		}
		return retDate;
	}

}
