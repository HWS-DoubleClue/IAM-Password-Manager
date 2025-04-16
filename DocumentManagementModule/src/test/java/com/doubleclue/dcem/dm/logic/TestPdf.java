package com.doubleclue.dcem.dm.logic;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;

import com.doubleclue.dcem.dm.preferences.DmPreferences;

class TestPdf {

	@Test
	void test() throws Exception {
		
		DmPreferences dmPreferences = new DmPreferences();
		dmPreferences.setTesseractDataPath("C:\\Tesseract-OCR\\tessdata");
		
		File pdfFile = File.createTempFile("testpdf-", ".pdf");
		PdfManagement pdfManagement = new PdfManagement();
		pdfManagement.createEmptyPdfFile(pdfFile);
	//	pdfManagement.dmPreferences = dmPreferences;
				
		BufferedImage bufferedImage = createImage("What a nice day");
		File outputfile1 = File.createTempFile("testpdf-", ".png");
		ImageIO.write(bufferedImage, "png", outputfile1);
		
		bufferedImage = createImage("Hello Document mangement");
		File outputfile2 = File.createTempFile("testpdf-", ".png");
		ImageIO.write(bufferedImage, "png", outputfile2);
		
		File pdfInput = createPdf();
		
//		pdfManagement.addImageToPdf(pdfFile, outputfile1);
//		pdfManagement.addImageToPdf(pdfFile, outputfile2);
		pdfManagement.mergePdf(pdfFile, pdfInput, null, outputfile1);
		System.out.println("TestPdf.test() PDF File: " + pdfFile);
	//	String result = pdfManagement.getOcrFromPdfFile(pdfFile);
	//	System.out.println("TestTess4.main() " + result);
	}
	
	@Test
	public File createPdf() throws IOException {
		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);
		PDPageContentStream contentStream = new PDPageContentStream(document, page);
		contentStream.beginText();
		contentStream.setFont(new PDType1Font (Standard14Fonts.FontName.COURIER), 20);
		contentStream.newLine();
		contentStream.showText("PDF-PDF-PDF");
		contentStream.endText();
		contentStream.close();
		File pdfInput = File.createTempFile("testpdf-test", ".pdf");
		document.save(pdfInput);
		document.close();
		System.out.println("TestTess4.createPdf:  " + pdfInput.getAbsolutePath());
		return pdfInput;
	}
	
	private BufferedImage createImage (String text) {
		BufferedImage bufferedImage = new BufferedImage(1200, 675, BufferedImage.TYPE_INT_RGB);
		Graphics g = bufferedImage.getGraphics();
		g.setFont(new Font("TimesRoman", Font.BOLD, 50));
		g.drawString(text, 20, 250);
		return bufferedImage; 
	}	
	
	@Test
	public void createNewPdfFile () {
		File pdfFile = null;
		try {
			pdfFile = File.createTempFile("DoubleClueDM-", ".pdf");
			PDDocument document = new PDDocument();
			PDPage blankPage = new PDPage();
			 document.addPage( blankPage );
			document.save(pdfFile);
			document.save("c:\\temp\\newPdf.pdf");
			document.close();
			System.out.println("TestPdf.createNewPdfFile() " + pdfFile.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// pdfFile.
	}

}
