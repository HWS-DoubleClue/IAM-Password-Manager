package com.doubleclue.dcem.app;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.doubleclue.dcem.core.gui.DcemApplicationBean;

public class JLogger {

	static public void setup(String logFile, int limit, int count) throws IOException {

		Logger logger = Logger.getLogger("");
		Handler fileHandler = new FileHandler(logFile, limit, count, true);
		fileHandler.setFormatter(new SimpleFormatter());
		fileHandler.setLevel(Level.INFO);
		fileHandler.setEncoding("UTF-8");
		Handler[] handlers = logger.getHandlers();

		if (DcemApplicationBean.debugMode == false) {
			if (handlers[0] instanceof ConsoleHandler) {
				logger.removeHandler(handlers[0]);
			}
		}
		logger.addHandler(fileHandler);

	}
}
