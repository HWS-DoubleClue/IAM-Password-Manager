package com.doubleclue.dcem.core.logging;

import java.io.File;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.LoggerConfig;

import com.doubleclue.dcem.core.config.LocalPaths;

/**
 * 
 * @author Emanuel Galea
 *
 */
public class LogUtils {

	public static final String DCEM_BASE_PACKAGE = "com.doubleclue";
	public static final String APPENDER_CONSOLE = "Console";

	public static Logger initLog4j(String defaultLogger, String additionalLoggers, DcemLogLevel dcemLogLevel, boolean withConsole) {
//
		File file;
		try {
			file = LocalPaths.getLog4JConfig();
			if (file != null && file.exists()) {
				Configurator.initialize(null, file.getAbsolutePath());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		Level level = Level.INFO;
		switch (dcemLogLevel) {
		case DEBUG:
			level = Level.DEBUG;
			break;
		case WARN:
			level = Level.WARN;
			break;
		case TRACE:
			level = Level.TRACE;
			break;
		default:
			break;
		}

//		if (withConsole == false) {  // EG Not not set the root Logger
//			setLevel(LogManager.getRootLogger(), level, withConsole);
//		}

		if (additionalLoggers != null && additionalLoggers.isEmpty() == false) {

			if (additionalLoggers.equalsIgnoreCase("all")) {
				setLevel(LogManager.getRootLogger(), level, withConsole);
			} else {
				String[] loggers = additionalLoggers.split(";");
				for (String logger : loggers) {
					if (logger.trim().length() > 0) {
						setLevel(LogManager.getLogger(logger.trim()), level, withConsole);
					}
				}
			}
		}
		if (defaultLogger == null) {
			defaultLogger = DCEM_BASE_PACKAGE;
		}
		Logger logger = LogManager.getLogger(defaultLogger);

		if (logger != null) {
			setLevel(logger, level, withConsole);
		}
		return logger;
	}

	private static Level setLevel(Logger log, Level level, boolean withConsole) {
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);

		Configuration conf = ctx.getConfiguration();
		LoggerConfig lconf = conf.getLoggerConfig(log.getName());

		if (withConsole == false) {
			lconf.removeAppender(APPENDER_CONSOLE); 
		}
		Level oldLevel = lconf.getLevel();
		lconf.setLevel(level);
		ctx.updateLoggers(conf);
		return oldLevel;
	}

}
