package com.doubleclue.dcem.core.utils.process;

public class ProcessResult {

	int processExitCode;
	String consoleOutput;
	Exception processException;

	public ProcessResult(int processExitCode, String consoleOutput, Exception processException) {
		super();
		this.processExitCode = processExitCode;
		this.consoleOutput = consoleOutput;
		this.processException = processException;
	}

	public int getProcessExitCode() {
		return processExitCode;
	}

	public void setProcessExitCode(int processExitCode) {
		this.processExitCode = processExitCode;
	}

	public String getConsoleOutput() {
		return consoleOutput;
	}

	public void setConsoleOutput(String consoleOutput) {
		this.consoleOutput = consoleOutput;
	}

	public Exception getProcessException() {
		return processException;
	}

	public void setProcessException(Exception processException) {
		this.processException = processException;
	}

	@Override
	public String toString() {
		return "ProcessResult [processExitCode=" + processExitCode + ", consoleOutput=" + consoleOutput + "]";
	}
}
