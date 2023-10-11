import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class AddTimeZone {
	public static void main(String args[]) {
		// Create a date time object with time zone for current date time and default time zone:
		ZonedDateTime todayWithDefaultTimeZone = ZonedDateTime.now();
		DateTimeFormatter formatTodayWithZoneOffset = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm:ss Z");
		
		System.out.format("Formatted date time with default zone offset is %s\n", todayWithDefaultTimeZone.format(formatTodayWithZoneOffset));
		DateTimeFormatter formatTodayWithZoneName = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm:ss z");
		System.out.format("Formatted date time with default zone name is %s\n", todayWithDefaultTimeZone.format(formatTodayWithZoneName));
		DateTimeFormatter formatTodayWithZoneId = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm:ss VV");
		System.out.format("Formatted date time with default zone ID is %s\n", todayWithDefaultTimeZone.format(formatTodayWithZoneId));

		// Create a zoned date time object with current date and time and New York time zone ID:
		ZoneId timeZone = ZoneId.of("America/New_York");
		LocalDateTime today = LocalDateTime.now();
		ZonedDateTime zonedToday = ZonedDateTime.now();
		ZonedDateTime zonedNewYork = zonedToday.withZoneSameInstant(timeZone);
		
		ZoneOffset currentOffsetForMyZone = timeZone.getRules().getOffset(today);
		OffsetDateTime newYork = today.atOffset(currentOffsetForMyZone);
	//	todayWithTimeZone
		//System.out.println("AddTimeZone.main() " + newYork);
	//	OffsetDateTime offsetDateTime = todayWithTimeZone.toOffsetDateTime();
	//	System.out.println("AddTimeZone.main( )" + offsetDateTime.format(formatTodayWithZoneId));
	//	System.out.format("Formatted date time with zone ID of New York is %s\n", todayWithTimeZone.format(formatTodayWithZoneId));
		
		System.out.println("AddTimeZone.main() " +  DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).format(zonedNewYork));

		// Create a zoned date time object with current date and time and zone offset UTC -5 hours:
		LocalDateTime todayZoneOffset = LocalDateTime.now();
		ZoneId timeZoneOffset = ZoneOffset.of("-0500");
		ZonedDateTime todayWithTimeZoneOffset = ZonedDateTime.of(todayZoneOffset, timeZoneOffset);
	 	System.out.format("Formatted date time with zone offset of UTC -5 hours is %s\n", newYork.format(formatTodayWithZoneOffset));
	}
}
