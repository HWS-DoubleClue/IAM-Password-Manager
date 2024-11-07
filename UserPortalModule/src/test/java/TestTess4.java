import java.io.File;
import java.util.Locale;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class TestTess4 {

	public static void main(String[] args) {
		File image = new File("C:\\Temp\\tesseract\\adidas.png"
				+ "");
		Tesseract tesseract = new Tesseract();
		tesseract.setDatapath("C:\\Tesseract-OCR\\tessdata");
		tesseract.setLanguage(Locale.ENGLISH.getISO3Language() + "+" +  Locale.GERMAN.getISO3Language());
		tesseract.setPageSegMode(1);
		tesseract.setOcrEngineMode(3);
	
		try {
			String result = tesseract.doOCR(image);
			System.out.println("TestTess4.main() " + result);
		} catch (TesseractException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

}
