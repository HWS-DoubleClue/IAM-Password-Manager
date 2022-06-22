package com.doubleclue.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceFinder {

	// private static final char PKG_SEPARATOR = '.';
	//
	// private static final char DIR_SEPARATOR = '/';

	// private static final String CLASS_FILE_SUFFIX = ".class";

	// private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package
	// '%s' exists?";

	public static List<FileContent> find(Class klass, String scannedPackage, String type)
			throws Exception {
		CodeSource src = klass.getProtectionDomain().getCodeSource();
		URL urlPath = src.getLocation();
		if (urlPath.getPath().endsWith("jar")) {
			return findInJar(urlPath, scannedPackage, type);
		} else {
			File scannedDir = new File(URLDecoder.decode(urlPath.getFile(), StandardCharsets.UTF_8.toString()), scannedPackage);
			return findInFile(scannedDir, type);
		}
	}

	private static List<FileContent> findInFile(File file, String type) throws IOException {
		List<FileContent> fileContents = new ArrayList<FileContent>();
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				fileContents.addAll(findInFile(child, type));
			}
		} else if (file.getName().endsWith(type)) {
			InputStream inputStream = new FileInputStream(file);
			byte[] data = KaraUtils.readInputStream(inputStream);
			fileContents.add(new FileContent(file.getName(), data));
		}
		return fileContents;
	}

	private static List<FileContent> findInJar(URL url, String scannedPackage, String type)
			throws Exception {
		List<FileContent> fileContents = new ArrayList<FileContent>();
		File file = new File(url.toURI());
		JarFile jarFile = new JarFile(file);
		final Enumeration<JarEntry> entries = jarFile.entries();
		InputStream inputStream;
		while (entries.hasMoreElements()) {
			final JarEntry jarEntry = entries.nextElement();
			String entryName = jarEntry.getName();
			if (entryName.startsWith(scannedPackage) && entryName.endsWith(type)) {
				String fileName = entryName.replace('/', '.');
				inputStream = jarFile.getInputStream(jarEntry);
				fileContents.add(new FileContent(fileName.substring(scannedPackage.length()+1), KaraUtils.readInputStream(inputStream)));
			}
		}
		jarFile.close();

		return fileContents;
	}

}
