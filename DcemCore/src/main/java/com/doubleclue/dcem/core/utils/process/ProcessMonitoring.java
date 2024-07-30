package com.doubleclue.dcem.core.utils.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
	public ProcessResult call() throws Exception {
		int exitCode = -1;
		String result = null;
		try {
			exitCode = process.waitFor();
		} catch (Exception e) {
			return new ProcessResult(exitCode, null, e);
		}
		if (outputFile == null) {
			StringBuilder stringBuilder = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
			String line;
			try {
				while ((line = in.readLine()) != null) {
					stringBuilder.append(line);
				}
			} catch (IOException e) {
			}
			result = stringBuilder.toString();			
		} else {
			byte[] fileContent = Files.readAllBytes(outputFile.toPath());
			Charset charset = Charset.defaultCharset();
			int offset = 0;
			if (fileContent.length > 2) {
				if (fileContent[0] == (byte)0xFF && fileContent[1] == (byte)0xFE) {
					charset = StandardCharsets.UTF_16LE;
					offset = 2;
				} else if (fileContent[0] == (byte)0xFE && fileContent[1] == (byte)0xFF) {
					charset = StandardCharsets.UTF_16BE;
					offset = 2;
				} else if (fileContent[0] == (byte)0xEF && fileContent[1] == (byte)0xBB && fileContent[2] == (byte)0xBF) {
					charset = StandardCharsets.UTF_8;
					offset = 3;
				}
			}
			result = new String(fileContent, offset, fileContent.length - offset, charset);
		}
		return new ProcessResult(exitCode, result, null);
	}
}
