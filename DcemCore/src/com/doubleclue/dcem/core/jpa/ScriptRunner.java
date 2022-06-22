/**
 *    Copyright 2009-2016 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.doubleclue.dcem.core.jpa;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.utils.KaraUtils;

/**
 * @author Clinton Begin
 */
public class ScriptRunner {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

	private static final String DEFAULT_DELIMITER = ";";

	private static final String METHOD = "@METHOD";

	final Logger logger = LogManager.getLogger(ScriptRunner.class);

	private Connection connection;

	private boolean stopOnError;
	private boolean throwWarning;
	private boolean removeCRs;
	private boolean escapeProcessing = true;

	private String delimiter = DEFAULT_DELIMITER;
	private boolean fullLineDelimiter;

	private int migrateFrom;
	private int migrateTo;
	boolean migration = false;
	DatabaseTypes dbType;

	public ScriptRunner(Connection connection) {
		this.connection = connection;
	}

	public void setStopOnError(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}

	public void setThrowWarning(boolean throwWarning) {
		this.throwWarning = throwWarning;
	}

	public void setSendFullScript(boolean sendFullScript) {
	}

	public void setRemoveCRs(boolean removeCRs) {
		this.removeCRs = removeCRs;
	}

	/**
	 * @since 3.1.1
	 */
	public void setEscapeProcessing(boolean escapeProcessing) {
		this.escapeProcessing = escapeProcessing;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public void setFullLineDelimiter(boolean fullLineDelimiter) {
		this.fullLineDelimiter = fullLineDelimiter;
	}

	public void runScript(Reader reader, DatabaseTypes dbType, int migrateFrom, int migrateTo) {
		this.migrateFrom = migrateFrom;
		this.migrateTo = migrateTo;
		this.dbType = dbType;
		if (migrateTo > 0) {
			migration = true;
		} else {
			migration = false;
		}
		StringBuilder command = new StringBuilder();
		try {
			BufferedReader lineReader = new BufferedReader(reader);
			String line;
			while ((line = lineReader.readLine()) != null) {
				command = handleLine(command, line);
			}
//			commitConnection();
			checkForMissingLineTerminator(command);
		} catch (Exception e) {
			String message = "Error executing: " + command + ".  Cause: " + e;
			logger.error(message);
			throw new RuntimeSqlException(message, e);
		}
	}

	public void closeConnection() {
		try {
			connection.close();
		} catch (Exception e) {
			// ignore
		}
	}


	private void checkForMissingLineTerminator(StringBuilder command) {
		if (command != null && command.toString().trim().length() > 0) {
			throw new RuntimeSqlException("Line missing end-of-line terminator (" + delimiter + ") => " + command);
		}
	}

	private StringBuilder handleLine(StringBuilder command, String line) throws SQLException, UnsupportedEncodingException {
		String trimmedLine = line.trim();
		if (lineIsComment(trimmedLine)) {
			final String cleanedString = trimmedLine.substring(2).trim().replaceFirst("//", "");
			if (cleanedString.toUpperCase().startsWith("@DELIMITER")) {
				delimiter = cleanedString.substring(11, 12);
				return command;
			}
			if (cleanedString.toUpperCase().startsWith(METHOD)) {
				executeMethod(cleanedString.substring(METHOD.length()));
				return command;
			}
			logger.info(trimmedLine);
		} else if (commandReadyToExecute(trimmedLine)) {
			command.append(line.substring(0, line.lastIndexOf(delimiter)));
			command.append(LINE_SEPARATOR);
			logger.info(command);
			executeStatement(command.toString());
			command.setLength(0);
		} else if (trimmedLine.length() > 0) {
			command.append(line);
			command.append(LINE_SEPARATOR);
		}
		return command;
	}

	private void executeMethod(String method) throws SQLException {
		int ind = KaraUtils.skipWhiteSpaces(method);
		if (ind == -1) {
			logger.warn("No whitespace found in method " + method);
			return;
		}
		method = method.substring(ind);
		int ind2 = KaraUtils.nextWhiteSpace(method);
		if (ind2 == -1) {
			ind2 = method.length();
		}
		String beanName = method.substring(0, ind2);
		String parameter = "";
		if (ind2 < method.length()) {
			parameter = method.substring(ind2 + 1, method.length());
		}
		MigrateDbI migrateDb = CdiUtils.getReference(beanName);
		if (migration) {
			migrateDb.migrate(connection, dbType, migrateFrom, migrateTo, parameter);
		} else {
			migrateDb.dbMethod(connection, dbType, parameter);
		}
	}

	private boolean lineIsComment(String trimmedLine) {
		return trimmedLine.startsWith("//") || trimmedLine.startsWith("--");
	}

	private boolean commandReadyToExecute(String trimmedLine) {
		// issue #561 remove anything after the delimiter
		return !fullLineDelimiter && trimmedLine.contains(delimiter) || fullLineDelimiter && trimmedLine.equals(delimiter);
	}

	private void executeStatement(String command) throws SQLException {
		boolean hasResults = false;
		Statement statement = connection.createStatement();
		statement.setEscapeProcessing(escapeProcessing);
		String sql = command;
		if (removeCRs) {
			sql = sql.replaceAll("\r\n", "\n");
		}
		if (stopOnError) {
			hasResults = statement.execute(sql);
			if (throwWarning) {
				// In Oracle, CRATE PROCEDURE, FUNCTION, etc. returns warning
				// instead of throwing exception if there is compilation error.
				SQLWarning warning = statement.getWarnings();
				if (warning != null) {
					throw warning;
				}
			}
		} else {
			try {
				hasResults = statement.execute(sql);
			} catch (SQLException e) {
				String message = "Error executing: " + command + ".  Cause: " + e;
				logger.error(message);
			}
		}
		printResults(statement, hasResults);
		try {
			statement.close();
		} catch (Exception e) {
			// Ignore to workaround a bug in some connection pools
		}
	}

	private void printResults(Statement statement, boolean hasResults) {
		try {
			if (hasResults) {
				ResultSet rs = statement.getResultSet();
				if (rs != null) {
					ResultSetMetaData md = rs.getMetaData();
					int cols = md.getColumnCount();
					for (int i = 0; i < cols; i++) {
						String name = md.getColumnLabel(i + 1);
						logger.info(name + "\t");
					}
					while (rs.next()) {
						for (int i = 0; i < cols; i++) {
							String value = rs.getString(i + 1);
							logger.info(value);
						}
					}
				}
			}
		} catch (SQLException e) {
			logger.error("Error printing results: " + e.getMessage(), e);
		}
	}

}
