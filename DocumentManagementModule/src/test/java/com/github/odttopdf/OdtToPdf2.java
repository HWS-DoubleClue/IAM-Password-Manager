/*
 * $Id$
 *
 * Copyright 2013 Valentyn Kolesnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.odttopdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.odftoolkit.odfdom.doc.OdfTextDocument;

import fr.opensagres.odfdom.converter.pdf.PdfConverter;
import fr.opensagres.odfdom.converter.pdf.PdfOptions;
import fr.opensagres.odfdom.converter.xhtml.XHTMLConverter;

/**
 * Converts ODT to PDF.
 *
 * @author Valentyn Kolesnikov
 * @version $Revision$ $Date$
 */
public class OdtToPdf2 {
	private static final String USAGE = "Usage: java -jar odttopdf.jar template.odt data.xml";


	public static void main(String[] args) {
		// if (args.length == 0) {
		// System.err.println(USAGE);
		// return;
		// }
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(new File("c:\\temp\\document.odt"));

			OdfTextDocument document = OdfTextDocument.loadDocument(inputStream);
			OutputStream out = new FileOutputStream( new File("c:\\temp\\document.html") );
			XHTMLConverter.getInstance().convert( document, out, null );
			
			OutputStream out2 = new FileOutputStream( new File("c:\\temp\\document.pdf") );
			 PdfOptions options = PdfOptions.create().fontEncoding( "UTF-8" );
			 PdfConverter.getInstance().convert( document, out2, options );
			System.out.println("OdtToPdf2.main() READY");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
