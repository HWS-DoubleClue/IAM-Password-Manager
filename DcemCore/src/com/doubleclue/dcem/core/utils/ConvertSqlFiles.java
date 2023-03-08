package com.doubleclue.dcem.core.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConvertSqlFiles {

	public static int countFiles = 0;
	public static int countNewDirectories = 0;

	final Logger logger = LogManager.getLogger(ConvertSqlFiles.class);

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Pleaser insert the input Directory in the arguments!");
			System.exit(-1);
		}
		// Read in the source folder
		File inputDirectory = new File(args[0]);
		if (inputDirectory.exists() == false) {
			System.err.println("Source folder is not existing");
			System.exit(-1);
		}
		// Read in the destination folder
		File outputDirectory = new File(args[1]);
		if (outputDirectory.exists() == false) {
			System.err.println("Destination folder is not existing");
			System.exit(-1);
		}
		try {
			convertSqlDirectories(inputDirectory, outputDirectory);
		} catch (Exception e) {
			System.exit(-1);
		}
	}

	public static void convertSqlDirectories(File inputdir, File outputdir) throws Exception {
		for (String dbDirectory : inputdir.list()) {
			File sqlInputDirectory = new File(inputdir, dbDirectory);
			File sqlOutputDirectory = new File(outputdir, dbDirectory);
			// Check if the destination File ist existing
			if (sqlOutputDirectory.exists() == false) {
				sqlOutputDirectory.mkdirs();
				countNewDirectories++;
			}
			// Put the file path together
			for (String sqlFileStr : sqlInputDirectory.list()) {
				File sqlFileInput = new File(sqlInputDirectory, sqlFileStr);
				File sqlFileOutput = new File(sqlOutputDirectory, sqlFileStr);
				try {
					// Call the convertFile method
					convertFile(sqlFileInput, sqlFileOutput);
					countFiles++;
				} catch (Exception e) {
					System.err.println("Please check your input file! File: " + sqlFileInput.getAbsolutePath());
					throw e;
				}
			}
		}

		System.out.println("Finished !!!");
		System.out.println("There were " + countFiles + " Files changed!");
		if (countNewDirectories > 1) {
			System.out.println("There were " + countNewDirectories + " new Folders created!");
		}

	} // main

	/**
	 * @param inputFile
	 * @param outputFile
	 * @throws Exception
	 *             converts your File
	 */
	private static void convertFile(File inputFile, File outputFile) throws Exception {

		BufferedReader bufferedReader = null;
		FileReader fileReader = null;
		boolean systemFile = false;
		// boolean testFile = false;
		if (outputFile.equals(inputFile)) {
			throw new Exception("Outputfile is the same as Inputfile");
		}
		if (outputFile.equals(inputFile)) {
			throw new Exception("Outputfile is the same as Inputfile");
		}
		fileReader = new FileReader(inputFile);
		bufferedReader = new BufferedReader(fileReader);

		if (inputFile.getPath().indexOf("dcem.systemTables") > 0) {
			systemFile = true;
		}
		String lineSeparator = System.getProperty("line.separator");
		FileWriter filewriter = new FileWriter(outputFile, false);
		BufferedWriter bufferedwriter = new BufferedWriter(filewriter);
		
		String zeile = "";
		String trimZeile;
		boolean copyLines = false;

		// if (inputFile.getPath().indexOf("POSTGRE") > 0) {
		// System.out.println("ConvertSqlFiles.convertFile() as");
		// }

		while (true) {
			zeile = bufferedReader.readLine();
			if (zeile == null) {
				break;
			}
			trimZeile = zeile.trim();
			if (trimZeile.isEmpty()) {
				continue;
			}
			if (trimZeile.indexOf("clob(255)") > 0) {
				trimZeile = trimZeile.replaceAll("clob\\(255\\)", "clob(10M)");
			}

			// if (trimZeile.contains("ts core_")) {
			// System.out.println("ConvertSqlFiles.convertFile()");
			// }

			if (trimZeile.startsWith("create table ") || trimZeile.startsWith("alter table ") || trimZeile.startsWith("insert into ")
					|| trimZeile.startsWith("create sequence ") || trimZeile.startsWith("create unique ") || trimZeile.startsWith("create index ")) { // Search
																																						// for
																																						// first
				if ((systemFile == false) && (trimZeile.contains(" on sys_") || trimZeile.contains(" on core_") || trimZeile.contains(" table core_")
						|| trimZeile.contains(" table sys_") || trimZeile.startsWith("insert into core_") || trimZeile.startsWith(" insert into sys_")
						|| trimZeile.contains("if exists core_") || trimZeile.contains("if exists sys_") || trimZeile.contains(".core_")
						|| trimZeile.contains(".sys_"))) {
					copyLines = false;
				} else {
					filewriter.write(lineSeparator);
					copyLines = true;
				}
				if (trimZeile.startsWith("alter table if exists")) { // most probably Postgre
					trimZeile = trimZeile.replace("alter table if exists", "alter table");
				}
			}
			if (copyLines == true) { // Transfer lines
				filewriter.write(trimZeile + System.lineSeparator());
			}
		}
		filewriter.close();
		bufferedwriter.close();
		fileReader.close();
		bufferedReader.close();
	}
}
