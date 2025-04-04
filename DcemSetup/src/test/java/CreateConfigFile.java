import java.io.IOException;
import java.util.Base64;

import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.DbPoolConfig;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.setup.logic.DbLogic;

public class CreateConfigFile {

	public static void main(String[] args) throws IOException {

		try {
			LocalPaths.getConfigurationFile();
		} catch (DcemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		byte [] key =  null;
		DbLogic dbLogic = new DbLogic();
		try {
			key = dbLogic.createDbKey();
		} catch (DcemException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(-1);
		}
		

		LocalConfig localConfig = new LocalConfig();
		DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setAdminName("root");
		databaseConfig.setIpAddress("localhost");
		databaseConfig.setAdminPassword("root");
		databaseConfig.setDatabaseName("dcem_db");
		databaseConfig.setDatabaseEncryptionKey(Base64.getEncoder().encodeToString(key));
		
		databaseConfig.setDatabaseType(DatabaseTypes.MARIADB.name());
		DbPoolConfig dbPoolConfig = new DbPoolConfig();
		//		nodeConfig.setNodeName("A");
		localConfig.setDatabase(databaseConfig);
		localConfig.setDbPoolConfig(dbPoolConfig);
		try {
			LocalConfigProvider.readConfig();
			LocalConfigProvider.writeConfig(localConfig);
		} catch (DcemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println("Configuration file created at " + LocalPaths.getConfigurationFile().getAbsolutePath());
		} catch (DcemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.exit(0);
	}

}