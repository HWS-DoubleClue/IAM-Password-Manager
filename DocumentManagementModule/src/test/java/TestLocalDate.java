import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class TestLocalDate {

	public static void main(String[] args) {
		DateTimeFormatter dateTimeFormatter =DateTimeFormatter.ofPattern("(dd-MM-yyyy HH-mm)");
		LocalDateTime ldt = LocalDateTime.now();
		String date = ldt.format(dateTimeFormatter);
		
		System.out.println("TestLocalDate.main() " + date);
	}

}
