package com.doubleclue.dcem.core.logging;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverDescription;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;


/*
 *    NOT IN USE * NOT IN USE * NOT IN USE * NOT IN USE 
 *       
 */

//org.apache.logging.log4j.core.config.plugins.Plugin(name = "DeleteMaxAgeFilesStrategy", category = "Core", printObject = true)
public class DeleteMaxAgeFilesStrategy implements RolloverStrategy {
	
	private static final Logger logger = LogManager.getLogger(DeleteMaxAgeFilesStrategy.class);

//	DefaultRolloverStrategy defaultRolloverStrategy;
	
	private static final int DEFAULT_MAX_AGE = 2;
	
	private int maxAgeIndex;

	public DeleteMaxAgeFilesStrategy(int maxAgeIndex) {
		this.maxAgeIndex = maxAgeIndex;
	}

	@Override
	public RolloverDescription rollover(RollingFileManager manager) throws SecurityException {
		purgeMaxAgeFiles(manager);
//		RolloverDescription rolloverDescription = null;
//		try {
//			rolloverDescription = defaultRolloverStrategy.rollover(manager);
//		} catch (Exception e) {
//			logger.warn("Error in rollover", e);
//		}
		return null;
	}

	@PluginFactory
	public static DeleteMaxAgeFilesStrategy createStrategy(@PluginAttribute("maxAge") final String maxAge) {

		int maxAgeIndex = DEFAULT_MAX_AGE;
	    if (maxAge != null) {
	        maxAgeIndex = Integer.parseInt(maxAge);
	    }
//		DefaultRolloverStrategy defaultRolloverStrategy = DefaultRolloverStrategy.createStrategy(max, min, fileIndex,
//				compressionLevelStr, config);
		return new DeleteMaxAgeFilesStrategy(maxAgeIndex);
	}

	/**
	 * Purge files older than defined in system advanced settings.
	 * If file older than current date - maxAge delete them or else keep it.
	 *
	 * @param manager     The RollingFileManager
	 */
	private void purgeMaxAgeFiles(final RollingFileManager manager) {

//		int maxAge = 1;		//TODO make it configurable			

		String filename = manager.getFileName();
		File file = new File(filename);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -maxAgeIndex);
		Date cutoffDate = cal.getTime();

		logger.debug("Delete old log files");
		if (file.getParentFile().exists()) {
			filename = file.getName().replaceAll("\\..*", "");

			File[] files = file.getParentFile().listFiles(new StartsWithFileFilter(filename, false));

			for (int i = 0; i < files.length; i++) {
				try {
					BasicFileAttributes attr = Files.readAttributes(files[i].toPath(), BasicFileAttributes.class);
					if (new Date(attr.lastModifiedTime().toMillis()).before(cutoffDate)) {
						files[i].delete();
						logger.info("Deleted log file: " + files[i]);
					}
				} catch (Exception e) {
					logger.warn("Unable to delete old log files at rollover", e);
				}
			}
		}
	}

	class StartsWithFileFilter implements FileFilter {
		private final String startsWith;
		private final boolean inclDirs;

		public StartsWithFileFilter(String startsWith, boolean includeDirectories) {
			super();
			this.startsWith = startsWith.toUpperCase();
			inclDirs = includeDirectories;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File pathname) {
			if (!inclDirs && pathname.isDirectory()) {
				return false;
			} else
				return pathname.getName().toUpperCase().startsWith(startsWith);
		}
	}
}
