package com.doubleclue.portaldemo.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * 
 * @author Emanuel Galea
 *
 */
public class LogUtils {

	public static final String DCEM_BASE_PACKAGE = "com.doubleclue";
	public static final String APPENDER_CONSOLE = "Console";

	public static Logger initLog4j(String defaultLogger, String additionalLoggers, Level level, boolean removeConsole) {
		

		if (removeConsole) {
			setLevel(LogManager.getRootLogger(), level, removeConsole);
		}
				

		if (additionalLoggers != null && additionalLoggers.isEmpty() == false) {

			if (additionalLoggers.equalsIgnoreCase("all")) {
				setLevel(LogManager.getRootLogger(), level, removeConsole);
			} else {
				String[] loggers = additionalLoggers.split(";");
				for (String logger : loggers) {
					if (logger.trim().length() > 0) {
						setLevel(LogManager.getLogger(logger.trim()), level, removeConsole);
					}
				}
			}

		}
		if (defaultLogger == null) {
			defaultLogger = DCEM_BASE_PACKAGE;
		}
		Logger logger = LogManager.getLogger(defaultLogger);
	
		if (logger != null) {
			setLevel(logger, level, removeConsole);
		}
		return logger;
	}

	private static Level setLevel(Logger log, Level level, boolean removeConsole) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
						
		Configuration conf = ctx.getConfiguration();
		LoggerConfig lconf = conf.getLoggerConfig(log.getName());
			
		if (removeConsole) {
			lconf.removeAppender(APPENDER_CONSOLE);
		}
		
		Level oldLevel = lconf.getLevel();
		lconf.setLevel(level);
//		ctx.updateLoggers(conf);
		return oldLevel;
	}

}
