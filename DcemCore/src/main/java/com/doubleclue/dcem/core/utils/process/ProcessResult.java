package com.doubleclue.dcem.core.utils.process;

public class ProcessResult {
	
	int exitCode;
	String consoleOutput;

	public ProcessResult(int exitCode, String consoleOutput) {
		super();
		this.exitCode = exitCode;
		this.consoleOutput = consoleOutput;
	}
	public int getExitCode() {
		return exitCode;
	}
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
	public String getConsoleOutput() {
		return consoleOutput;
	}
	public void setConsoleOutput(String consoleOutput) {
		this.consoleOutput = consoleOutput;
	}
	@Override
	public String toString() {
		return "ProcessResult [exitCode=" + exitCode + ", consoleOutput=" + consoleOutput + "]";
	}
	
	

}
