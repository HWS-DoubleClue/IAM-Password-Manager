package com.doubleclue.dcem.core.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.ViewVariable;

public class ExportTsv {

	static byte[] CRLF = new byte[] { (byte) 0x0a, 0x0d };

	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");

	Class<?> klass;

	List<ViewVariable> viewVariables;

	ZipOutputStream zipOutputStream;
	
	FileOutputStream fileOutputStream;
	

	public String start (Class<?> klass) throws DcemException, IOException {
		this.klass = klass;
		File archiveDir = LocalPaths.getArchiveDirectory();
		File file = new File(archiveDir, klass.getSimpleName() + dateFormat.format(new Date()) + ".zip");
		file.createNewFile();
		fileOutputStream = new FileOutputStream(file);
		viewVariables = DcemUtils.getViewVariables(klass, null, null, null);
		zipOutputStream = new ZipOutputStream(fileOutputStream);

		ZipEntry zipEntry = new ZipEntry(klass.getSimpleName() + ".tsv");
		zipOutputStream.putNextEntry(zipEntry);
		for (ViewVariable viewVariable : viewVariables) {
			zipOutputStream.write(viewVariable.getDisplayName().getBytes(DcemConstants.CHARSET_UTF8));
			zipOutputStream.write('\t');
		}
		zipOutputStream.write(CRLF);
		return file.getAbsolutePath();
	}

	public void addList(List<EntityInterface> list) throws DcemException, UnsupportedEncodingException, IOException {

		for (EntityInterface entityInterface : list) {
			for (ViewVariable viewVariable : viewVariables) {
				zipOutputStream.write(viewVariable.getRecordData(entityInterface).getBytes(DcemConstants.CHARSET_UTF8));
				zipOutputStream.write('\t');
			}
			zipOutputStream.write(CRLF);
		}

	}

	public void close()  {
		try {
		zipOutputStream.closeEntry();
		zipOutputStream.close();
		fileOutputStream.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
