package com.doubleclue.dcem.dm.logic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.doubleclue.dcem.dm.preferences.DmPreferences;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
//import com.lowagie.text.Row;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import net.sourceforge.tess4j.Tesseract;

@ApplicationScoped
public class PdfManagement {

	@Inject
	protected DocumentManagementModule documentManagementModule;

	Tesseract tesseract = null;

	private void initialzeTesseract() throws Exception {
		DmPreferences dmPreferences = documentManagementModule.getMasterPreferences();
		if (dmPreferences.getTesseractDataPath() == null || dmPreferences.getTesseractDataPath().isEmpty()) {
			throw new DcemException(DcemErrorCodes.OCR_TESSERACT_NOT_CONFIGURED, "");
		}
		tesseract = new Tesseract();
		tesseract.setDatapath(dmPreferences.getTesseractDataPath());
		tesseract.setLanguage(Locale.ENGLISH.getISO3Language() + "+" + Locale.GERMAN.getISO3Language());
		tesseract.setPageSegMode(1);
		tesseract.setOcrEngineMode(3);
	}

	public void createEmptyPdfFile(File file) throws IOException {
		PDDocument document = new PDDocument();
		// PDPage blankPage = new PDPage();
		// document.addPage(blankPage);
		document.save(file);
		document.close();
	}

	/**
	 * @param contentFile
	 * @param file
	 * @return number of pages
	 * @throws IOException
	 */
	public int addImageToPdf(File inputFile, File outputFile, byte[] imageData, String name) throws Exception {
		PDDocument document = Loader.loadPDF(inputFile);
		ByteArrayInputStream inputStream = new ByteArrayInputStream(imageData);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		float width = bufferedImage.getWidth();
		float height = bufferedImage.getHeight();
		PDPage page = new PDPage(new PDRectangle(width + 10, height + 10));
		document.addPage(page);
		System.out.println("PdfManagement.addImageToPdf() size " + width + " / " + height);
		PDImageXObject imageXObject = PDImageXObject.createFromByteArray(document, imageData, name);
		PDPageContentStream contentStream = new PDPageContentStream(document, page, AppendMode.APPEND, true, false);
		float scale = 1f;
		contentStream.drawImage(imageXObject, 10, 10, imageXObject.getWidth() * scale, imageXObject.getHeight() * scale);
		contentStream.close();
		inputStream.close();
		document.save(outputFile);
		document.close();
		return document.getNumberOfPages();
	}

	public int mergePdf(File pdfFile, File sourcefile, InputStream mergeInputStream, File outpuFile) throws Exception {
		PDFMergerUtility pdfmerger = new PDFMergerUtility();
		PDDocument document = Loader.loadPDF(pdfFile);
		PDDocument source;
		if (sourcefile != null) {
			source = Loader.loadPDF(sourcefile);
		} else {
			source = Loader.loadPDF(new RandomAccessReadBuffer(mergeInputStream));
		}
		pdfmerger.appendDocument(document, source);
		document.save(outpuFile);
		document.close();
		source.close();
		return document.getNumberOfPages();
	}

	public int getNoOfPages(File pdfFile) throws IOException {
		PDDocument document = Loader.loadPDF(pdfFile);
		int pages = document.getNumberOfPages();
		document.close();
		return pages;
	}

	public int deletePages(File pdfFile, File outputFile, int pageFrom, int pageTo) throws Exception {
		PDDocument document = Loader.loadPDF(pdfFile);
		int pages = document.getNumberOfPages();
		if (pageFrom < 1 || pageFrom > pages) {
			throw new DcemException(DcemErrorCodes.PAGE_NOT_FOUND, null);
		}
		if (pageTo < pageFrom) {
			throw new DcemException(DcemErrorCodes.PAGE_NOT_FOUND, null);
		}
		pageFrom--;
		pageTo--;
		for (int i = 0; i < (pageTo - pageFrom) + 1; i++) {
			document.removePage(pageFrom);
		}
		document.save(outputFile);
		document.close();
		return document.getNumberOfPages();
	}

	public byte[] createThumbnail(File pdfFile, Dimension dimension) throws Exception {
		PDDocument document = Loader.loadPDF(pdfFile);
		PDFRenderer renderer = new PDFRenderer(document);
		// scale = scale * Toolkit.getDefaultToolkit().getScreenResolution() / 72.0f;
		BufferedImage bufferedImage = renderer.renderImage(0, 1);
		bufferedImage = DcemUtils.resizeImage(bufferedImage, dimension.width, dimension.height, false);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		ImageIO.write(bufferedImage, "jpg", baos);
		return baos.toByteArray();
	}

	public String getOcrText(File file, DcemMediaType dcemMediaType) throws Exception {
		if (tesseract == null) {
			initialzeTesseract();
		}
		String result = null;
		try {
			switch (dcemMediaType) {
			case JPEG:
			case PNG:
			case GIF:
				BufferedImage bufferedImage = ImageIO.read(file);
				result = tesseract.doOCR(bufferedImage);
				break;
			case PDF:
				PDDocument document = Loader.loadPDF(file);
				result = extractTextFromPDF(document, tesseract);
				break;
			case SVG:
				return "";
			default:
				result = tesseract.doOCR(file);
			}
		} catch (Throwable e) {
			throw new DcemException(DcemErrorCodes.OCR_TESSERACT_ERROR, "", e);
		}
		return result;
	}

	private String extractTextFromPDF(PDDocument document, Tesseract tesseract) throws Exception {
		StringBuilder sb = new StringBuilder();
		PDPageTree list = document.getPages();
		PDFTextStripper stripper = new PDFTextStripper();
		PdfExtractImage pdfExtractImage = new PdfExtractImage();
		int pageIndex;
		for (PDPage page : list) {
			pageIndex = list.indexOf(page) + 1;
			stripper.setStartPage(pageIndex);
			stripper.setEndPage(pageIndex);
			sb.append(stripper.getText(document));
			pdfExtractImage.clearListOfImages();
			pdfExtractImage.processPage(page);
			List<BufferedImage> images = pdfExtractImage.getListOfImages();
			for (BufferedImage bufferedImage : images) {
				sb.append(tesseract.doOCR(bufferedImage));
			}
		}
		return sb.toString();
	}

	public List<Integer> removeEmptyPages(File inputFile, File outputFile) throws Exception {
		PDDocument document = Loader.loadPDF(inputFile);
		PDPageTree list = document.getPages();
		PDFTextStripper stripper = new PDFTextStripper();
		PdfExtractImage pdfExtractImage = new PdfExtractImage();
		int pageIndex;
		List<Integer> blankPageList = new ArrayList<>();
		for (PDPage page : list) {
			pageIndex = list.indexOf(page) + 1;
			stripper.setStartPage(pageIndex);
			stripper.setEndPage(pageIndex);
			if (stripper.getText(document).isBlank() == false) {
				continue;
			}
			pdfExtractImage.clearListOfImages();
			pdfExtractImage.processPage(page);
			if (pdfExtractImage.getListOfImages().isEmpty()) {
				blankPageList.add(pageIndex - 1);
			}
		}
		for (Integer i : blankPageList) {
			document.removePage(i);
		}
		document.save(outputFile);
		document.close();
		return blankPageList;
	}

	// public void convertHtmlToPDF(File htmlFile, File pdfFile) throws Exception {
	// FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
	// PdfRendererBuilder builder = new PdfRendererBuilder();
	// builder.withUri(htmlFile.toURI().toURL().toString());
	// builder.toStream(fileOutputStream);
	// builder.run();
	// }

	public void convertHtmlToPDF(File htmlFile, File pdfFile) throws Exception {
		FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.useFastMode();
		Document doc = Jsoup.parse(htmlFile);
		org.w3c.dom.Document dom = new W3CDom().fromJsoup(doc);
		builder.withW3cDocument(dom, null);
		builder.toStream(fileOutputStream);
		builder.run();
		fileOutputStream.close();
	}

	public void convertHtmlStringToPDF(String html, File pdfFile) throws Exception {
		FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.useFastMode();
		Document doc = Jsoup.parse(html);
		org.w3c.dom.Document dom = new W3CDom().fromJsoup(doc);
		builder.withW3cDocument(dom, null);
		builder.toStream(fileOutputStream);
		builder.run();
		fileOutputStream.close();
	}

	public void convertWordToPDF(File wordFile, File pdfFile) throws Exception {
		InputStream doc = new FileInputStream(wordFile);
		XWPFDocument document = new XWPFDocument(doc);
		PdfOptions options = PdfOptions.create();
		OutputStream out = new FileOutputStream(pdfFile);
		PdfConverter.getInstance().convert(document, out, options);
		out.close();
		doc.close();
	}
	
	public void convertOdtToPDF(File odtFile, File pdfFile) throws Exception {
		InputStream inputStream = new FileInputStream(odtFile);
		OdfTextDocument odfDocument = OdfTextDocument.loadDocument(inputStream);
		fr.opensagres.odfdom.converter.pdf.PdfOptions options = fr.opensagres.odfdom.converter.pdf.PdfOptions.create();
		OutputStream out = new FileOutputStream(pdfFile);
		fr.opensagres.odfdom.converter.pdf.PdfConverter.getInstance().convert(odfDocument, out, options);
		out.close();
		inputStream.close();
	}

	public void convertExcelToPdf(File excelFile, File pdfFile) throws Exception {
		com.lowagie.text.Document document = new com.lowagie.text.Document();
		PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
		document.open();
		Workbook workbook = new XSSFWorkbook(excelFile);
		Sheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		int columnNo = 0;
		String cellValue;
		List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
		int endColumn = getTableWidth(sheet, mergedRegions);
		PdfPTable pdfPTable = new PdfPTable(endColumn);
		pdfPTable.setWidthPercentage(100);
		int rowNo;
		Row row;
		MyCell[] myCells = new MyCell[endColumn];
		while (rowIterator.hasNext()) {
			row = rowIterator.next();
			rowNo = row.getRowNum();
			// Iterator<Cell> cellIterator = row.cellIterator();
			columnNo = 0;
			CellStyle rowStyle = row.getRowStyle();
			PdfPCell emptyPdfPCell = new PdfPCell(new Phrase(""));
			if (rowStyle != null) {
				setBackgroundColor(rowStyle, emptyPdfPCell);
			}
			for (int i = 0; i < endColumn; i++) { // Empty row
				myCells[i] = new MyCell();
			}

			for (columnNo = 0; columnNo < endColumn; columnNo++) {
				Cell cell = row.getCell(columnNo);
				if (cell == null) {
					myCells[columnNo].emptyCell = true;
					continue;
				}
				int cellNo = cell.getAddress().getColumn();
				CellStyle cellStyle = cell.getCellStyle();
				CellFormat cf = CellFormat.getInstance(cellStyle.getDataFormatString());
				CellFormatResult result = cf.apply(cell);
				cellValue = result.text;
				// System.out.println("Row " + row.getRowNum() + ", Column: " + cell.getAddress().getColumn() + ", Value: " + cellValue);
				PdfPCell pdfCell = new PdfPCell(new Phrase(cellValue, getCellStyle(cell)));
				// Check if merged Region
				CellRangeAddress cellRangeAddress = getCellRangeAddress(mergedRegions, cell);
				if (cellRangeAddress != null) {
					// System.out.println("ExcelToPDF.excelToPdf() " + cellRangeAddress + " Value: " + cellValue);
					if (cellNo == cellRangeAddress.getFirstColumn() && rowNo == cellRangeAddress.getFirstRow()) {
						int colSpan = (cellRangeAddress.getLastColumn() - columnNo) + 1;
						pdfCell.setColspan(colSpan);
						// int rowSpan = (cellRangeAddress.getLastRow() - rowNo) + 1;
						// if (rowSpan > 1) {
						// pdfCell.setRowspan(rowSpan);
						// System.out.println("Row Span: " + rowSpan);
						// }
						// spanIgnoreMax = cellRangeAddress.getLastColumn() + 1;
						// spanIgnoreMin = cellRangeAddress.getFirstColumn() + 1;
					} else {
						// only colspan
						if (rowNo != cellRangeAddress.getFirstRow() && cellNo == cellRangeAddress.getFirstColumn()) {
							PdfPCell pdfPCell = new PdfPCell(new Phrase(""));
							pdfPCell.setColspan((cellRangeAddress.getLastColumn() - columnNo) + 1);
							myCells[columnNo].pdfPCell = pdfPCell;
						} else {
							myCells[columnNo].spanCell = true;
						}
						continue;
					}
				}
				setBackgroundColor(cellStyle, pdfCell);
				setCellAlignment(cell, pdfCell);
				myCells[columnNo].pdfPCell = pdfCell;
			}
			// end of row
			if (isEmptyRow(myCells) == true) {
				continue; // skip empty rows
			}
			for (MyCell myCell : myCells) {
				if (myCell.emptyCell == true) {
					pdfPTable.addCell(emptyPdfPCell);
				} else if (myCell.spanCell == true) {
					continue;
				} else {
					pdfPTable.addCell(myCell.pdfPCell);
				}
			}
			pdfPTable.completeRow();
		}
		document.add(pdfPTable);
		document.close();
		workbook.close();
	}

	private boolean isEmptyRow(MyCell[] myCells) {
		boolean emptyRow = true;
		for (MyCell myCell : myCells) {
			if (myCell.pdfPCell != null && myCell.pdfPCell.getPhrase().getContent().isBlank() == false) {
				emptyRow = false;
				break;
			}
		}
		return emptyRow;

	}

	private CellRangeAddress getCellRangeAddress(List<CellRangeAddress> mergedRegions, Cell cell) {
		for (CellRangeAddress rangeAddress : mergedRegions) {
			if (rangeAddress.isInRange(cell) == true) {
				return rangeAddress;
			}
		}
		return null;
	}

	private int getTableWidth(Sheet sheet, List<CellRangeAddress> mergedRegions) {
		Iterator<Row> iter = sheet.rowIterator();
		int firstColumn = (iter.hasNext() ? Integer.MAX_VALUE : 0);
		int endColumn = 0;
		while (iter.hasNext()) {
			Row row = iter.next();
			short firstCell = row.getFirstCellNum();
			if (firstCell >= 0) {
				firstColumn = Math.min(firstColumn, firstCell);
				int lastNonEmptyColumn = row.getLastCellNum() - 1;
				// System.out.println("ExcelToPDF.getTableWidth() " + lastNonEmptyColumn + " row " + );
				int lastColumn = lastNonEmptyColumn;
				Cell cell;
				// System.out.println("ExcelToPDF.getTableWidth() ROW: " + row.getRowNum() + " widht: " + lastNonEmptyColumn);
				while (lastNonEmptyColumn >= 0) {
					cell = row.getCell(lastNonEmptyColumn);
					if (cell == null) {
						lastNonEmptyColumn--;
						lastColumn--;
						continue;
					}
					CellRangeAddress cellRangeAddress = getCellRangeAddress(mergedRegions, cell);
					if (cellRangeAddress != null) {
						lastNonEmptyColumn = cellRangeAddress.getLastColumn();
						if (lastNonEmptyColumn == lastColumn) {
							break;
						}
					}
					if (cell != null) {
						CellFormat cf = CellFormat.getInstance(cell.getCellStyle().getDataFormatString());
						CellFormatResult result = cf.apply(cell);
						if (result.text.isBlank() == false) {
							break;
						} else {
							lastNonEmptyColumn--;
							lastColumn--;
						}
					}
				}
				endColumn = Math.max(endColumn, lastNonEmptyColumn);
			}
		}
		return endColumn + 1;
	}

	private void setCellAlignment(Cell cell, PdfPCell cellPdf) {
		CellStyle cellStyle = cell.getCellStyle();

		HorizontalAlignment horizontalAlignment = cellStyle.getAlignment();
		switch (horizontalAlignment) {
		case LEFT:
			cellPdf.setHorizontalAlignment(Element.ALIGN_LEFT);
			break;
		case CENTER:
			cellPdf.setHorizontalAlignment(Element.ALIGN_CENTER);
			break;
		case JUSTIFY:
		case FILL:
			cellPdf.setVerticalAlignment(Element.ALIGN_JUSTIFIED);
			break;
		case RIGHT:
			cellPdf.setHorizontalAlignment(Element.ALIGN_RIGHT);
			break;
		}
	}

	private void setBackgroundColor(CellStyle cellStyle, PdfPCell cellPdf) {
		short bgColorIndex = cellStyle.getFillForegroundColor();
		if (bgColorIndex != IndexedColors.AUTOMATIC.getIndex()) {
			XSSFColor bgColor = (XSSFColor) cellStyle.getFillForegroundColorColor();
			if (bgColor != null) {
				byte[] rgb = bgColor.getRGB();
				if (rgb != null && rgb.length == 3) {
					cellPdf.setBackgroundColor(new Color(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
				}
			}
		}
	}

	private Font getCellStyle(Cell cell) throws DocumentException, IOException {
		Font font = new Font();
		CellStyle cellStyle = cell.getCellStyle();
		org.apache.poi.ss.usermodel.Font cellFont = cell.getSheet().getWorkbook().getFontAt(cellStyle.getFontIndexAsInt());
		if (cellFont.getItalic()) {
			font.setStyle(Font.ITALIC);
		}
		if (cellFont.getStrikeout()) {
			font.setStyle(Font.STRIKETHRU);
		}
		if (cellFont.getUnderline() == 1) {
			font.setStyle(Font.UNDERLINE);
		}
		short fontSize = cellFont.getFontHeightInPoints();
		font.setSize(fontSize);
		if (cellFont.getBold()) {
			font.setStyle(Font.BOLD);
		}
		String fontName = cellFont.getFontName();
		if (FontFactory.isRegistered(fontName)) {
			font.setFamily(fontName);
		} else {
			font.setFamily("Helvetica");
		}
		return font;
	}
}

class MyCell {
	boolean emptyCell;
	boolean spanCell;
	PdfPCell pdfPCell;
}
