import java.io.File;
import java.io.FileOutputStream;


import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;


public class ExcelToPDF2 {

	public static void main(String[] args) {

		try {
			File excelFile = new File("c:\\temp\\excel2.xlsx");
			File pdfFile = new File("c:\\temp\\excel2.pdf");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	static public void convertHtmlToPDF(Document document, File pdfFile) throws Exception {
//		FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
//		PdfRendererBuilder builder = new PdfRendererBuilder();
//		builder.useFastMode();
////		org.jsoup.nodes.Document doc = Jsoup.parse(htmlFile);
////		org.w3c.dom.Document dom = new W3CDom().fromJsoup(doc);
//		builder.withW3cDocument(document, null);
//		builder.toStream(fileOutputStream);
//		builder.run();
//	}

	

}
