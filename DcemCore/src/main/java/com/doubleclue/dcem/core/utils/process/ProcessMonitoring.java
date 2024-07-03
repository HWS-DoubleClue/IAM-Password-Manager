package com.doubleclue.dcem.core.utils.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;


public class ProcessMonitoring implements Callable<ProcessResult> {

	Process process;
	
	public ProcessMonitoring(Process process) {
		super();
		this.process = process;
	}

	@Override
	public ProcessResult call() throws Exception {
		StringBuffer stringBuffer = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		try {
			while ((line = in.readLine()) != null) {
				stringBuffer.append(line);
			}
		} catch (IOException e) {
		}
		int exitCode = -1;
		try {
			exitCode =  process.waitFor();
		} catch (InterruptedException e) {
		}
		try {
			in.close();
		} catch (IOException e) {
		}
		return new ProcessResult(exitCode,  stringBuffer.toString());
	}	
}
