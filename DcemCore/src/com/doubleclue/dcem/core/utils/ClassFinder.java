package com.doubleclue.dcem.core.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassFinder {

	private static final Logger logger = LogManager.getLogger(ClassFinder.class);

	private static final char PKG_SEPARATOR = '.';

	private static final char DIR_SEPARATOR = '/';

	private static final String CLASS_FILE_SUFFIX = ".class";

	private static final String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the package '%s' exists?";

	public static List<Class<?>> find(String scannedPackage)
			throws FileNotFoundException, ClassNotFoundException, IOException {
		String scannedPath = scannedPackage.replace(PKG_SEPARATOR, DIR_SEPARATOR);
		URL scannedUrl = Thread.currentThread().getContextClassLoader().getResource(scannedPath);
		if (scannedUrl == null) {
			throw new IllegalArgumentException(String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage));
		}
		String newUrl = scannedUrl.toString().replace("%20", " ");
		scannedUrl = new URL(newUrl);
		
		if (scannedUrl.getProtocol().startsWith("jar")) {
			return find(scannedUrl, scannedPackage);
		}
		logger.info("Scann URL: " + scannedUrl.toString());
		File scannedDir = new File(scannedUrl.getFile());
		List<Class<?>> classes = new ArrayList<Class<?>>();

		for (File file : scannedDir.listFiles()) {
			classes.addAll(find(file, scannedPackage));
		}

		return classes;
	}

	private static List<Class<?>> find(File file, String scannedPackage) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		String resource = scannedPackage + PKG_SEPARATOR + file.getName();
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				classes.addAll(find(child, resource));
			}
		} else if (resource.endsWith(CLASS_FILE_SUFFIX)) {
			int endIndex = resource.length() - CLASS_FILE_SUFFIX.length();
			String className = resource.substring(0, endIndex);
			try {
				classes.add(Class.forName(className));
			} catch (ClassNotFoundException ignore) {
			}
		}
		return classes;
	}

	private static List<Class<?>> find(URL url, String scannedPackage)
			throws FileNotFoundException, IOException, ClassNotFoundException {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		JarURLConnection urlCon = (JarURLConnection) (url.openConnection());
		JarFile jarFile = urlCon.getJarFile();
		// int ind = fileName.lastIndexOf('!');
		// fileName = fileName.substring(0, ind);
		// JarInputStream crunchifyJarFile = new JarInputStream(jarFile.);
		Enumeration<JarEntry> entries = jarFile.entries();
		JarEntry jarEntry;
		scannedPackage = scannedPackage.replace('.', '/');
		while (entries.hasMoreElements()) {
			jarEntry = entries.nextElement();
			if (jarEntry.getName().startsWith(scannedPackage)) {
				System.out.println("ClassFinder.find() " + jarEntry.getName());
				if (jarEntry.getName().endsWith("class")) {
					try {
						String className = jarEntry.getName().replace('/', '.');
						className = className.substring(0, className.length()-6);
						classes.add(Class.forName(className));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

		}
		return classes;
	}

}
