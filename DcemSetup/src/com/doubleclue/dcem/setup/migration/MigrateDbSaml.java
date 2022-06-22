package com.doubleclue.dcem.setup.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.jpa.MigrateDbI;

@ApplicationScoped
@Named("MigrateDbSaml")
public class MigrateDbSaml implements MigrateDbI {

	private static final Logger logger = LogManager.getLogger(MigrateDbSaml.class);

	@Override
	public void migrate(Connection connection, DatabaseTypes dbType, int from, int to, String parameter) throws SQLException {
		System.out.println("MigrateDbAs.migrate() " + parameter);
		switch (to) {
		case 3:
			migrateTo_3(connection);
			break;
		}
	}

	private void migrateTo_3(Connection connection) throws SQLException {
		logger.info("migrating saml to DB-Version 2");
		PreparedStatement selectStatement = connection.prepareStatement("SELECT * FROM core_textMessage WHERE dc_key LIKE \"saml.%\"");
		PreparedStatement updateStatement = connection.prepareStatement("UPDATE core_textMessage SET dc_key=? WHERE dc_key=?");
		ResultSet rs = selectStatement.executeQuery();
		while (rs.next()) {
			String key = rs.getString(3);
			updateStatement.setString(1, key.replaceFirst("saml", "sso"));
			updateStatement.setString(2, key);
			updateStatement.execute();
		}
		updateStatement.close();
		selectStatement.close();
	}

	@Override
	public void dbMethod(Connection connection, DatabaseTypes dbType, String parameter) throws SQLException {
	}
}
