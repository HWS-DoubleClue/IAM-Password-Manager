package com.doubleclue.dcem.dm.logic;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * This is an example on how to extract images from pdf.
 */
public class PdfExtractImage extends PDFStreamEngine {
	
	List<BufferedImage> listOfImages = new ArrayList<BufferedImage>();;
	/**
	 * Default constructor.
	 *
	 * @throws IOException If there is an error loading text stripper properties.
	 */
	public PdfExtractImage() throws IOException {
	}

	/**
	 * @param operator The operation to perform.
	 * @param operands The list of arguments.
	 *
	 * @throws IOException If there is an error processing the operation.
	 */
	@Override
	protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
		String operation = operator.getName();
		if ("Do".equals(operation)) {
			COSName objectName = (COSName) operands.get(0);
			PDXObject xobject = getResources().getXObject(objectName);
			if (xobject instanceof PDImageXObject) {
				PDImageXObject image = (PDImageXObject) xobject;
				listOfImages.add(image.getImage());
			} 
		} else {
			super.processOperator(operator, operands);
		}
	}

	public List<BufferedImage> getListOfImages() {
		return listOfImages;
	}
	
	public void clearListOfImages() {
		listOfImages.clear();
	}

	

}
