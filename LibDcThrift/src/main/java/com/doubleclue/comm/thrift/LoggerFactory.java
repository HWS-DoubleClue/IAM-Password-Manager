package com.doubleclue.comm.thrift;

public class LoggerFactory {

	public static Logger getLogger(String loggerName) {
		return new Logger(loggerName);

	}

}
