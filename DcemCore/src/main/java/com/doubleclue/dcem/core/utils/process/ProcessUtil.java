package com.doubleclue.dcem.core.utils.process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.doubleclue.utils.KaraUtils;

public class ProcessUtil {

	// this is used as old Windows console still uses Character-Encoding ASCII-850 whcih is obsolte since mote than 20 years :-)
	/**
	* @param resourcePath  This is the path for a Java resource path. using '/' as package seperator
	* @param invoke       Set this to null, if script shall run on same machine. Else build up the invoke command
	* @param parameters    parameters for the ps script
	* @param timeoutSeconds      If process will take longer, a TimeoutException will be thrown
	* @return  ProcessResult
	* @throws Exception
	*/
	public static ProcessResult executePowerShell(String resourcePath, String invoke, String[] parameters, int timeoutSeconds) throws Exception {
		File tempFile = copyResourceToTempFile(resourcePath, ".ps1");
		List<String> commandList = new ArrayList<>();
		commandList.add("powershell.exe");
		if (invoke != null) {
			commandList.add(invoke);
		}
		commandList.add("-File");
		commandList.add(tempFile.getAbsolutePath());
		for (int i = 0; i < parameters.length; i++) {
			commandList.add(parameters[i]);
		}

		ProcessResult processResult = executeProcess(commandList, timeoutSeconds);
		tempFile.delete();
		return processResult;
	}

	/**
	* @param commandList command Name and parameters
	* @param timeoutSeconds If process will take longer a TimeoutException will be thrown
	* @return  ProcessResult
	* @throws Exception
	*/
	public static ProcessResult executeProcess(List<String> commandList, int timeoutSeconds) throws Exception {
		File outputFile = createOutputTempFile(".txt");
		commandList.add(">");
		commandList.add(outputFile.getAbsolutePath());
		ProcessBuilder processBuilder = new ProcessBuilder(commandList);
		processBuilder.command(commandList);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		ProcessMonitoring processMonitoring = new ProcessMonitoring(process, outputFile);
		Future<ProcessResult> future = scheduler.submit(processMonitoring);
		ProcessResult processResult = future.get(timeoutSeconds, TimeUnit.SECONDS);
		scheduler.shutdown();
		outputFile.delete();
		return processResult;
	}

	private static File copyResourceToTempFile(String resourcePath, String suffex) throws Exception {
		InputStream inputStream = ProcessUtil.class.getResourceAsStream(resourcePath);
		File tempFile = File.createTempFile("dcemProcess_", suffex);
		FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
		KaraUtils.copyStream(inputStream, fileOutputStream, 1024 * 64);
		fileOutputStream.close();
		inputStream.close();
		return tempFile;
	}

	private static File createOutputTempFile(String suffix) throws IOException {
		return File.createTempFile("powershell_output_", suffix);
	}
}
