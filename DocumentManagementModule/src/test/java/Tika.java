import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;

class Tika {

	@Test
	void test() {
		File file = new File("c:\\temp\\ProjectCharter_UserManagement.docx");
		try {


		//	DcemMediaType dcemMediaType = com.doubleclue.dcem.core.utils.typedetector.FileUploadDetector.detectDcemMediaType(file);

			System.out.println("Tika.test()");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
