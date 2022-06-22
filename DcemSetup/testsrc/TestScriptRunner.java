import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;

import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.jpa.JdbcUtils;
import com.doubleclue.dcem.core.jpa.ScriptRunner;

public class TestScriptRunner {

	public static void main(String[] args) {

		DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setJdbcUrl("jdbc:mysql://localhost:3306");
		databaseConfig.setDatabaseType("MARIADB");
		Connection connection = null;
		try {
			 connection = JdbcUtils.getJdbcConnection(databaseConfig, "root", "root");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		ScriptRunner runner = new ScriptRunner(connection);
		
		StringReader stringReader = new StringReader("--@METHOD MigrateDbCore param\n--\n");
		
		runner.runScript(stringReader,DatabaseTypes.MARIADB,0,1 );
		
		System.out.println("TestScriptRunner.main()");
		System.exit(0);
		

	}

}
