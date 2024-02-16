
import java.util.HashMap;
import java.util.Map;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
 
public class MySQLDatabaseGeneratorCopy {
 
    public static void main(String[] args) {
        Map<String, String> settings = new HashMap<>();
        settings.put("connection.driver_class", "com.mysql.jdbc.Driver");
        settings.put("dialect", "org.hibernate.dialect.MySQL57InnoDBDialect");
        settings.put("hibernate.connection.url", "jdbc:mysql://localhost/testdb?useSSL=false");
        settings.put("hibernate.connection.username", "root");
        settings.put("hibernate.connection.password", "");
        settings.put("hibernate.hbm2ddl.auto", "create");
        settings.put("show_sql", "true");
 
        MetadataSources metadata = new MetadataSources(
                new StandardServiceRegistryBuilder()
                        .applySettings(settings)
                        .build());
//        metadata.addAnnotatedClass(User.class);
//        metadata.addAnnotatedClass(Task.class);
//        SchemaExport schemaExport = new SchemaExport(
//                (MetadataImplementor) metadata.buildMetadata()
//        );
//        schemaExport.setHaltOnError(true);
//        schemaExport.setFormat(true);
//        schemaExport.setDelimiter(";");
//        schemaExport.setOutputFile("db-schema.sql");
//        schemaExport.execute(true, true, false, true);
    }
}
