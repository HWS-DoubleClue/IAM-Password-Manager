package com.doubleclue.dcem.core.jpa;

import java.sql.Connection;
import java.sql.SQLException;

public interface MigrateDbI {
	public void migrate (Connection connection, DatabaseTypes dbType, int from, int to, String parameter) throws SQLException;
	public void dbMethod (Connection connection, DatabaseTypes dbType, String parameter) throws SQLException;
}