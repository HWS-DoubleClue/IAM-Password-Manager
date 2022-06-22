import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AllTimeZones {

	public static void main(String[] args) {
		for (String timeZone : TimeZone.getAvailableIDs()) {
			System.out.println("AllTimeZones.main() " + timeZone);
		}

		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.FULL, Locale.GERMAN);
//		DateFormat df = DateFormat.get
		df.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
		System.out.println("AllTimeZones.main() " + df.format(new Date()));

	}

}
