package com.doubleclue.dcem.core.utils.process;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProcessUtilTest {

	@Test
	void test() {
		System.setProperty("file.encoding", "UTF-8");
		String [] cmd = new String [] {"cmd.exe", "/c", "dir", "c:\\temp"};
		try {
			ProcessResult result = ProcessUtil.executeProcess(cmd, 10);
			System.out.println("ProcessUtilTest.test() " + result.getConsoleOutput());
		} catch (Exception e) {
			e.printStackTrace();
		}
		fail("Not yet implemented");
	}

}
