package com.doubleclue.dcem.core.utils.process;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ProcessUtilTest {

	@Test
	void test() {
	//	System.setProperty("file.encoding", "UTF-8");
		List<String> cmdList = new ArrayList<>();
		cmdList.add("cmd.exe");
		cmdList.add("/c");
		cmdList.add("dir");
		cmdList.add("c:\\Temp");
		try {
			ProcessResult result = ProcessUtil.executeProcess(cmdList, 100);
			if (result.processExitCode != 0) {
				System.out.println("ProcessUtilTest.test() FAILED Code: " + result.processExitCode);
			}
			System.out.println("ProcessUtilTest.test() " + result.getConsoleOutput());
		} catch (Exception e) {
			e.printStackTrace();
		}
		fail("Not yet implemented");
	}
	
	@Test
	void testPowerShell () {
		//powershell "Resolve-DnsName 10.129.37.143 -ErrorAction Stop| Where-Object {$_.Type -eq 'PTR'} | Select-Object -ExpandProperty NameHost"
		System.setProperty("file.encoding", "UTF-8");
		List<String> cmdList = new ArrayList<>();
		cmdList.add("powershell.exe");
		cmdList.add("\"Resolve-DnsName 10.16.40.203 -ErrorAction Stop| Where-Object {$_.Type -eq 'PTR'} | Select-Object -ExpandProperty NameHost\"");
		try {
			ProcessResult result = ProcessUtil.executeProcess(cmdList, 30);
			if (result.processExitCode != 0) {
				System.out.println("ProcessUtilTest.test() FAILED Code: " + result.processExitCode);
			}
			System.out.println("ProcessUtilTest.test() " + result.getConsoleOutput());
		} catch (Exception e) {
			e.printStackTrace();
		}
		fail("Not yet implemented");
	}

}
