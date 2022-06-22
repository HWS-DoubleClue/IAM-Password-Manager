package com.doubleclue.dcem.boot;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.logging.DcemLogLevel;
import com.doubleclue.dcem.core.logging.LogUtils;

public class LogTest {



	public static void main(String[] args) {

		System.setProperty(LocalPaths.ENV_DCEM_LOGS, new File("c:\\temp\\logs", LocalPaths.LOGS).getAbsolutePath());
		LogUtils.initLog4j(null, null, DcemLogLevel.DEBUG, true);
		Logger logger = LogManager.getLogger(LogTest.class);
		for (int i = 0; i < 1024; i++) {
			logger.info("abcdefghijklmnoip arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9 0231874723190874921087984079213749719238");
		}
		System.out.println("LogTest.main():  READY");
		System.exit(0);
		

	}

}
