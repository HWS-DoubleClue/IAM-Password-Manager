import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class ExcelToPDF {

	public static void main(String[] args) {

		try {
			excelToPdf("c:\\temp\\onboarding.xlsx", "c:\\temp\\onboarding.pdf");
			System.out.println("ExcelToPDF.main() READY");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static void excelToPdf(String excelFile, String pdfFile) throws Exception {
		Document document = new Document();
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
		int spanIgnoreMin = 0;
		int spanIgnoreMax = 0;
		Row row;
		MyCell[] myCells = new MyCell[endColumn];
		while (rowIterator.hasNext()) {
			row = rowIterator.next();
			rowNo = row.getRowNum();
			System.out.println("ExcelToPDF.excelToPdf() Row:  " + rowNo + ", getLastCellNum: " + row.getLastCellNum());
			// Iterator<Cell> cellIterator = row.cellIterator();
			columnNo = 0;
			spanIgnoreMin = -1;
			spanIgnoreMax = 0;
			CellStyle rowStyle = row.getRowStyle();
			PdfPCell emptyPdfPCell = new PdfPCell(new Phrase(""));
			if (rowStyle != null) {
				setBackgroundColor(rowStyle, emptyPdfPCell);
			}
			for (int i = 0; i < endColumn; i++) { // Empty row
				myCells[i] = new MyCell();
			}

			for (columnNo = 0; columnNo < endColumn; columnNo++) {
				// while (cellIterator.hasNext()) {
				if (columnNo == endColumn) {
					break;
				}
				Cell cell = row.getCell(columnNo);
				if (cell == null) {
					myCells[columnNo].emptyCell = true;
					System.out.println("Column Skiped: " + columnNo);
					continue;
				}

				int cellNo = cell.getAddress().getColumn();
				System.out.println("Column: " + cell.getAddress());
				// if (cellNo >= spanIgnoreMin && cellNo < spanIgnoreMax) {
				// columnNo = cellNo + 1;
				// continue;
				// }
				CellStyle cellStyle = cell.getCellStyle();
				CellFormat cf = CellFormat.getInstance(cellStyle.getDataFormatString());
				CellFormatResult result = cf.apply(cell);
				cellValue = result.text;
				System.out.println("Row " + row.getRowNum() + ", Column: " + cell.getAddress().getColumn() + ", Value: " + cellValue);
				PdfPCell pdfCell = new PdfPCell(new Phrase(cellValue, getCellStyle(cell)));
				// Check if merged Region
				CellRangeAddress cellRangeAddress = getCellRangeAddress(mergedRegions, cell);
				if (cellRangeAddress != null) {
					System.out.println("ExcelToPDF.excelToPdf() " + cellRangeAddress + " Value: " + cellValue);
					if (cellNo == cellRangeAddress.getFirstColumn() && rowNo == cellRangeAddress.getFirstRow()) {
						int colSpan = (cellRangeAddress.getLastColumn() - columnNo) + 1;
						pdfCell.setColspan(colSpan);
//						int rowSpan = (cellRangeAddress.getLastRow() - rowNo) + 1;
//						if (rowSpan > 1) {
//							pdfCell.setRowspan(rowSpan);
//							System.out.println("Row Span: " + rowSpan);
//						}
						// spanIgnoreMax = cellRangeAddress.getLastColumn() + 1;
						// spanIgnoreMin = cellRangeAddress.getFirstColumn() + 1;
					} else {
						// only colspan
						if (rowNo != cellRangeAddress.getFirstRow() && cellNo == cellRangeAddress.getFirstColumn()) {
							PdfPCell pdfPCell = new PdfPCell(new Phrase(""));
							pdfPCell.setColspan((cellRangeAddress.getLastColumn() - columnNo) + 1);
							myCells[columnNo].pdfPCell = pdfPCell;
							System.out.println("span cell with Adding: " + cell.getAddress());
						} else {
							System.out.println("Ignore: " + cell.getAddress());
							myCells[columnNo].spanCell = true;
						}
						continue;
					}
				}
				System.out.println("ExcelToPDF.excelToPdf() Adding: " + cell.getAddress());
				setBackgroundColor(cellStyle, pdfCell);
				setCellAlignment(cell, pdfCell);
				myCells[columnNo].pdfPCell = pdfCell;

				// pdfPTable.addCell(pdfCell);
				// lastPdfPCell = pdfCell;
				// columnNo = cellNo + 1;
			}
			// end of row
			 if (isEmptyRow(myCells) == true) {
				 continue; // skip empty rows
			 }
			System.out.println("end of row: " + rowNo);
			for (MyCell myCell : myCells) {
				if (myCell.emptyCell == true) {
					pdfPTable.addCell(emptyPdfPCell);
				} else if (myCell.spanCell == true) {
					continue;
				} else {
					try {
						pdfPTable.addCell(myCell.pdfPCell);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			// while (columnNo < endColumn) {
			// System.out.println("Filling columnNo: " + columnNo);
			// pdfPTable.addCell(emptyPdfPCell);
			// columnNo++;
			// }
			pdfPTable.completeRow();
			System.out.println();
		}
		document.add(pdfPTable);
		document.close();
		workbook.close();
	}

	private static boolean isEmptyRow(MyCell[] myCells) {
		boolean emptyRow = true;
		for (MyCell myCell : myCells) {
			if (myCell.pdfPCell != null && myCell.pdfPCell.getPhrase().getContent().isBlank() == false) {
				emptyRow = false;
				break;
			}
		}
		return emptyRow;

	}

	private static CellRangeAddress getCellRangeAddress(List<CellRangeAddress> mergedRegions, Cell cell) {
		for (CellRangeAddress rangeAddress : mergedRegions) {
			if (rangeAddress.isInRange(cell) == true) {
				return rangeAddress;
			}
		}
		return null;
	}

	private static int getTableWidth(Sheet sheet, List<CellRangeAddress> mergedRegions) {
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

	static void setCellAlignment(Cell cell, PdfPCell cellPdf) {
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

	static void setBackgroundColor(CellStyle cellStyle, PdfPCell cellPdf) {
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

	static Font getCellStyle(Cell cell) throws DocumentException, IOException {
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
