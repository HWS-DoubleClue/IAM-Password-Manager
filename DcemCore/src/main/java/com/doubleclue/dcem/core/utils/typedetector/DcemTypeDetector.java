package com.doubleclue.dcem.core.utils.typedetector;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tika.Tika;

public class DcemTypeDetector {

	public static String detectMediaType(File file) throws Exception {
		Tika tika = new Tika();
		return tika.detect(file);
	}

	public static boolean isMediaTypeAllowed(File file, Collection<String> allowedTypes) throws Exception {
		if (file == null || allowedTypes == null || allowedTypes.isEmpty()) {
			return false;
		}
		String mediaType = detectMediaType(file);
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
}