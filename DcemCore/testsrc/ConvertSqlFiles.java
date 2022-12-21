import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConvertSqlFiles {

	public static int countFiles = 0;
	public static int countNewDirectories = 0;

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
		System.exit(0);

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
					e.printStackTrace();
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
		boolean coreFile = false;
		boolean systemFile = false;
		// boolean testFile = false;
		try {
			fileReader = new FileReader(inputFile);
			bufferedReader = new BufferedReader(fileReader);
		} catch (Exception e) {
			System.err.println("InputFile not found. File: " + inputFile);
			System.exit(-1);
		}

		if (inputFile.getPath().indexOf("dcem.system") > 0) {
			systemFile = true;
		}

		if (inputFile.getPath().indexOf("dcem.core") > 0) {
			coreFile = true;
		}
		if (outputFile.equals(inputFile)) {
			System.err.println("Outputfile is the same as Inputfile");

			System.exit(-1);
		}
		try {
			FileWriter filewriter = new FileWriter(outputFile, false);
			BufferedWriter bufferedwriter = new BufferedWriter(filewriter);

			if (outputFile.equals(inputFile)) {
				System.err.println("Outputfile is the same as Inputfile");
				System.exit(-1);
			}

			String zeile = "";
			String trimZeile;
			boolean copyLines = false;
			boolean createTableFound = false;

			while (true) {
				zeile = bufferedReader.readLine();
				if (zeile == null) {
					break;
				}
				trimZeile = zeile.trim();

				if (true) {
					if (trimZeile.indexOf("clob(255)") > 0) {
						trimZeile = trimZeile.replaceAll("clob\\(255\\)", "clob(10M)");

					}
				}

				if (coreFile || systemFile) {
					if (trimZeile.startsWith("create table ")) { // Search for first line
						copyLines = true;
					}
				} else {
					if (trimZeile.startsWith("create table ")) { // Search for first line
						createTableFound = true;
					}

					if (trimZeile.contains(" on sys_") && coreFile == false) { // Search for on sys_
						copyLines = false;
					} else if ((trimZeile.contains(" on as_") || trimZeile.contains(" on radiuss_")) && coreFile == false) { // Search for on as_ , if no
																																// coreFile
						copyLines = true;
					} else if (trimZeile.contains(" on core_") && coreFile == false) { // Search for on_core , if no coreFile
						copyLines = false;
					} else if ((trimZeile.contains(" on as_") || trimZeile.contains(" on radiuss_")) && coreFile == true) { // Search for on_as , if no coreFile
						copyLines = false;
					} else if (trimZeile.contains(" on core_") && coreFile == true) { // Search for on_core, if no coreFile
						copyLines = true;
					} else if (trimZeile.contains(" on licence_")) { // Search for on_licence
						copyLines = true;
					} else if (trimZeile.contains(" on dispatcher_")) { // Search for on_licence
						copyLines = true;
					} else if (trimZeile.contains(" on saml_")) { // Search for on saml
						copyLines = true;
					} else if (trimZeile.contains(" on otp_")) { // Search for on saml
						copyLines = true;
					} else if (trimZeile.contains(" on oauth_")) { // Search for on saml
						copyLines = true;
					} else if (trimZeile.contains(" on petshop_")) { // Search for on saml
						copyLines = true;
					} else if (trimZeile.contains(" on shifts_")) { // Search for on Shifts
						copyLines = true;
					} else if (trimZeile.contains(" on pd_")) { // Search for on Performance Deck
						copyLines = true;
					} else if (trimZeile.contains(" on crm_")) { // Search for on Shifts
						copyLines = true;
					} else if (trimZeile.contains(" on up_")) {  // Search for on userportal
						copyLines = true;
					} else if (trimZeile.contains(" on ftp_")) { // Search for on userportal
						copyLines = true;
					}

					if (trimZeile.startsWith("create table as_") || trimZeile.startsWith("create table test_") || trimZeile.startsWith("create table radius_")
							|| trimZeile.startsWith("create table dispatcher_") || trimZeile.startsWith("create table licence_")
							|| trimZeile.startsWith("create table otp_") || trimZeile.startsWith("create table oauth_")
							|| trimZeile.startsWith("create table petshop_") || trimZeile.startsWith("create table up_")
							|| trimZeile.startsWith("create table crm_")
							|| trimZeile.startsWith("create table pd_")
							|| trimZeile.startsWith("create table ftp_")
							|| trimZeile.startsWith("create sequence up_")
							|| trimZeile.startsWith("create sequence ftp_")
							|| trimZeile.startsWith("create table shifts_") ||trimZeile.startsWith("create table saml_")) {
						copyLines = true;
					}
					if (trimZeile.startsWith("alter table if exists")) { // most probably Postgre
						trimZeile = trimZeile.replace("alter table if exists", "alter table");
					}

					if ((trimZeile.startsWith("alter table as") || trimZeile.startsWith("alter table radius")) && createTableFound == true
							|| trimZeile.startsWith("alter table test") && createTableFound == true // Search for alter table test
							|| trimZeile.startsWith("alter table dispatcher") && createTableFound == true // Search for alter table test
							|| trimZeile.startsWith("alter table licence") && createTableFound == true // Search for alter table licence
							|| trimZeile.startsWith("alter table otp") && createTableFound == true // Search for alter table otp
							|| trimZeile.startsWith("alter table oauth") && createTableFound == true // Search for alter table oauth
							|| trimZeile.startsWith("alter table petshop") && createTableFound == true // Search for alter table oauth
							|| trimZeile.startsWith("alter table up") && createTableFound == true // Search for alter table user portal
							|| trimZeile.startsWith("alter table shifts") && createTableFound == true // Search for alter table user portal
							|| trimZeile.startsWith("alter table crm") && createTableFound == true  // Search for alter table saml
							|| trimZeile.startsWith("alter table pd") && createTableFound == true // Search for alter table performance deck
							|| trimZeile.startsWith("alter table ftp") && createTableFound == true // Search for alter table performance deck
							|| trimZeile.startsWith("alter table saml") && createTableFound == true) { // Search for alter table saml
						copyLines = true;
					} else if (trimZeile.startsWith("create table core_") || trimZeile.startsWith("alter table sys_")
							|| trimZeile.startsWith("alter table core_")) {
						// Search for create table core_ or alter table sys_ or alter table core_
						copyLines = false;
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

		catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.print("Couldn't convert files");
			e.printStackTrace();
			System.exit(-1);
		}

	}// convertFile
}
