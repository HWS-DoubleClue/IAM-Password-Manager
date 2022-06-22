import java.io.File;
import java.io.IOException;

import com.doubleclue.dcem.userportal.logic.MyApplication;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MyApplicationDeSer {

	public static void main(String[] args) {
		ObjectMapper objectMappaer = new ObjectMapper();
		try {
			MyApplication myApp = objectMappaer.readValue(new File ("C:\\Users\\emanuel.galea\\git\\DoubleClueMaster\\dcem\\DcemCore\\resources\\com\\doubleclue\\dcem\\myApplications\\Zoom.dcMyApp"), MyApplication.class);
			System.out.println("MyApplicationDeSer.main() " + myApp);
			objectMappaer.writeValue (new File ("c:\\temp\\zoom.dcMyApp"), myApp);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
