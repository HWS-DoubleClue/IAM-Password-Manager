package com.doubleclue.dcem.core.utils.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class ProcessMonitoring implements Callable<ProcessResult> {

	Process process;
	File outputFile;

	public ProcessMonitoring(Process process, File outputFile) {
		super();
		this.process = process;
		this.outputFile = outputFile;
	}

	@Override
	public ProcessResult call() throws UnsupportedEncodingException {
		int exitCode = -1;
		try {
			exitCode = process.waitFor();
		} catch (Exception e) {
			return new ProcessResult(exitCode, null, e);
		}
		StringBuffer stringBuffer = new StringBuffer();
		try (FileInputStream fileInputStream = new FileInputStream(outputFile);
				// Standard Charset for .txt
				InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_16LE);
				BufferedReader in = new BufferedReader(inputStreamReader)) {
			//removing leading BOM character
			in.mark(1);
			int firstChar = in.read();
			if (firstChar != 0xFEFF) {
				in.reset();
			}
			String line;
			while ((line = in.readLine()) != null) {
				stringBuffer.append(line);
			}
		} catch (IOException e) {
			return new ProcessResult(exitCode, null, e);
		}
		return new ProcessResult(exitCode, stringBuffer.toString(), null);
	}
}
