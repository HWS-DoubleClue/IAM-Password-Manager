package com.doubleclue.dcem.core.utils.typedetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.utils.StreamUtils;
import com.doubleclue.utils.KaraUtils;
import com.google.common.io.CharStreams;

public class FileUploadDetector {
	
	static Tika tika = new Tika();

	public static String detectMediaType(File file) throws Exception {
		return tika.detect(file);
	}

	public static DcemMediaType detectDcemMediaType(InputStream inputStream) throws Exception {
		String mediaType = tika.detect(inputStream);
		DcemMediaType dcemMediaType = DcemMediaType.getDcemMediaType(mediaType);
		return dcemMediaType;
	}

	public static Reader parseTika(File file, Metadata metadata) throws Exception {
		return tika.parse(file, metadata);
	}

	public static DcemMediaType detectDcemMediaType(File file) throws Exception {
		Metadata metadata = new Metadata();
		FileInputStream fileInputStream = new FileInputStream(file);

		// Reader reader = tika.parse(file, metadata);
		// char [] buffer = new char [8096];
		// int lenght = reader.read(buffer);
		// System.out.println("FileUploadDetector.detectDcemMediaType() " + new String (buffer));
		// reader.close();

		String mediaType = tika.detect(fileInputStream, metadata);
		DcemMediaType dcemMediaType = DcemMediaType.getDcemMediaType(mediaType);
		fileInputStream.close();
		return dcemMediaType;
	}

	public static String parseFile(File file, Metadata metadata) throws Exception {
		Reader reader = tika.parse(file, metadata);
		String targetString = IOUtils.toString(reader);
		reader.close();
		return targetString;
	}

	public static String detectMediaType(InputStream inputStream) throws Exception {
		return tika.detect(inputStream);
	}

	public static boolean isMediaTypeAllowed(File file, Collection<String> allowedTypes) throws Exception {
		if (file == null || allowedTypes == null || allowedTypes.isEmpty()) {
			return false;
		}
		String mediaType = detectMediaType(file);
		return validateMediaType(mediaType, allowedTypes);
	}

	public static boolean isMediaTypeAllowed(InputStream inputStream, Collection<String> allowedTypes) throws Exception {
		if (inputStream == null || allowedTypes == null || allowedTypes.isEmpty()) {
			return false;
		}
		String mediaType = detectMediaType(inputStream);
		return validateMediaType(mediaType, allowedTypes);
	}

	private static boolean validateMediaType(String mediaType, Collection<String> allowedTypes) throws Exception {
		String subType = getSubType(mediaType);
		for (String allowedType : allowedTypes) {
			if (mediaType.equalsIgnoreCase(allowedType) || allowedType.toLowerCase().endsWith(subType.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	private static String getSubType(String mediaType) {
		int slashIndex = mediaType.lastIndexOf('/');
		return mediaType.substring(slashIndex + 1);
	}

	public static boolean isMediaTypeAllowedWithDcemTypes(InputStream inputStream, Collection<DcemMediaType> allowedTypes) throws Exception {
		if (inputStream == null || allowedTypes == null || allowedTypes.isEmpty()) {
			return false;
		}
		Set<String> allowedTypesAsString = allowedTypes.stream().map(type -> type.getMediaType()).collect(Collectors.toSet());
		return isMediaTypeAllowed(inputStream, allowedTypesAsString);
	}

	public static boolean isMediaTypeAllowedWithDcemTypes(InputStream inputStream, DcemMediaType[] allowedTypes) throws Exception {
		if (inputStream == null || allowedTypes == null || allowedTypes.length == 0) {
			return false;
		}
		Set<String> allowedTypesAsString = Arrays.stream(allowedTypes).map(type -> type.getMediaType()).collect(Collectors.toSet());
		return isMediaTypeAllowed(inputStream, allowedTypesAsString);
	}

	public static boolean isMediaTypeAllowedWithDcemTypes(File file, Collection<DcemMediaType> allowedTypes) throws Exception {
		if (file == null || allowedTypes == null || allowedTypes.isEmpty()) {
			return false;
		}
		Set<String> allowedTypesAsString = allowedTypes.stream().map(type -> type.getMediaType()).collect(Collectors.toSet());
		return isMediaTypeAllowed(file, allowedTypesAsString);
	}

	public static boolean isMediaTypeAllowedWithDcemTypes(File file, DcemMediaType[] allowedTypes) throws Exception {
		if (file == null || allowedTypes == null || allowedTypes.length == 0) {
			return false;
		}
		Set<String> allowedTypesAsString = Arrays.stream(allowedTypes).map(type -> type.getMediaType()).collect(Collectors.toSet());
		return isMediaTypeAllowed(file, allowedTypesAsString);
	}

	public static DcemUploadFile getUploadedFile(String fileName, InputStream inputStream, DcemMediaType[] allowedMediaTypes) throws DcemException {
		File tempFile = null;
		FileOutputStream outputStream = null;
		try {
			tempFile = File.createTempFile("dcem-", "-fileUpload");
			outputStream = new FileOutputStream(tempFile);
			KaraUtils.copyStream(inputStream, outputStream);
		} catch (IOException e) {
			throw new DcemException(DcemErrorCodes.UNABLE_TO_UPLOAD_FILE, fileName, e);
		} finally {
			StreamUtils.close(inputStream);
			StreamUtils.close(outputStream);
		}
		try {
			boolean validType = FileUploadDetector.isMediaTypeAllowedWithDcemTypes(tempFile, allowedMediaTypes);
			if (validType == false) {
				throw new DcemException(DcemErrorCodes.INVALID_FILE_FORMAT, getSupportedFormats(allowedMediaTypes));
			}
		} catch (Exception e) {
			// logger.error("Unable to detect fileformat " + tempFile.getName(), e);
			throw new DcemException(DcemErrorCodes.INVALID_FILE_FORMAT, getSupportedFormats(allowedMediaTypes));
		}
		return new DcemUploadFile(fileName, tempFile);
	}

	static private String getSupportedFormats(DcemMediaType[] allowedMediaTypes) {
		StringBuilder sb = new StringBuilder();
		for (DcemMediaType dcemMediaType : allowedMediaTypes) {
			sb.append(dcemMediaType.name());
			sb.append(" ");
		}
		return sb.toString();
	}

}