import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class PdfTable2 {

	
	public static void main(String[] args) throws IOException {
	    PDDocument document = new PDDocument();
	    PDPage page = new PDPage();
	    document.addPage(page);

//	    int pageWidth = (int)page.getTrimBox().getWidth(); //get width of the page
//	    int pageHeight = (int)page.getTrimBox().getHeight(); //get height of the page

	    PDPageContentStream contentStream = new PDPageContentStream(document,page);
	    contentStream.setStrokingColor(Color.DARK_GRAY);
	    contentStream.setLineWidth(1);

	    int initX = 50;
	    int initY = 500-50;
	    int cellHeight = 20;
	    int cellWidth = 100;

	    int colCount = 3;
	    int rowCount = 3;

	    for(int i = 1; i<=rowCount;i++){
	        for(int j = 1; j<=colCount;j++){
	            if(j == 2){
	                contentStream.addRect(initX,initY,cellWidth+30,-cellHeight);

	                contentStream.beginText();
	                contentStream.newLineAtOffset(initX+30,initY-cellHeight+10);
	        //        contentStream.setFont(PDType1Font.,10);
	                contentStream.showText("Dinuka");
	                contentStream.endText();

	                initX+=cellWidth+30;
	            }else{
	                contentStream.addRect(initX,initY,cellWidth,-cellHeight);

	                contentStream.beginText();
	                contentStream.newLineAtOffset(initX+10,initY-cellHeight+10);
	               
	        		contentStream.setFont(new PDType1Font (Standard14Fonts.FontName.COURIER), 20);
	                contentStream.showText("Dinuka");
	                contentStream.endText();

	                initX+=cellWidth;
	            }
	        }
	        initX = 50;
	        initY -=cellHeight;
	    }

	    contentStream.stroke();
	    contentStream.close();


	    document.save("C:\\temp\\table.pdf");
	    document.close();
	    System.out.println("table pdf created");
	}
}
