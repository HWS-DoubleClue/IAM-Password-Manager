package com.doubleclue.dcem.core.utils.process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.doubleclue.utils.KaraUtils;

public class ProcessUtil {

//	public static void main(String[] args) {
//		String [] parameters = new String [] {"vc01-nbg-w01.adidas.noris.de", "prdsdcapp43345"};
//		try {
//			ProcessResult processResult = executePoweShell("/com/doubleclue/dcem/lsc/powershell/Vcenter.ps1" , parameters, 30);
//			System.out.println("PowerShellUtil.main() " +  processResult);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("PowerShellUtil.main() READY");
//		System.exit(0);
//	}

	public static ProcessResult executePowerShell(String resourcePath, String invoke,  String [] parameters, int timeoutSeconds) throws Exception {
		File tempFile = copyResourceToTempFile(resourcePath, ".ps1");
		List<String> commandList = new ArrayList <> ();
		commandList.add("powershell.exe");
		if (invoke != null) {
			commandList.add(invoke);
		}
		commandList.add("-File");
		commandList.add(tempFile.getAbsolutePath());
		for (int i = 0; i < parameters.length; i++) {
			commandList.add(parameters[i]);
		}
		ProcessResult processResult = executeProcess(commandList.toArray(new String[0]), timeoutSeconds);
		tempFile.delete();
		return processResult;
	}

	public static ProcessResult executeProcess(String[] commandList, int timeoutSeconds) throws Exception {
		ProcessBuilder processBuilder = new ProcessBuilder(commandList);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		ProcessMonitoring processMonitoring = new ProcessMonitoring(process);
		Future<ProcessResult> future = scheduler.submit(processMonitoring);
		ProcessResult processResult = future.get(timeoutSeconds, TimeUnit.SECONDS);
		scheduler.shutdown();
		return processResult;
	}

	public static File copyResourceToTempFile(String resourcePath, String suffex) throws Exception {
		InputStream inputStream = ProcessUtil.class.getResourceAsStream(resourcePath);
		File tempFile = File.createTempFile("dcemProcess_", suffex);
		FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
		KaraUtils.copyStream(inputStream, fileOutputStream, 1024 * 64);
		fileOutputStream.close();
		inputStream.close();
		return tempFile;
	}
}
