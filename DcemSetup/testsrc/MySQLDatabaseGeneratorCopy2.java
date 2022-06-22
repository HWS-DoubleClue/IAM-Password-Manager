
import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
 
public class MySQLDatabaseGeneratorCopy2 {
 
    public static void main(String[] args) {
        Map<String, String> settings = new HashMap<>();
        settings.put("connection.driver_class", "com.mysql.jdbc.Driver");
        settings.put("dialect", "org.hibernate.dialect.MySQL57InnoDBDialect");
        settings.put("hibernate.connection.url", "jdbc:mysql://localhost/testdb?useSSL=false");
        settings.put("hibernate.connection.username", "root");
        settings.put("hibernate.connection.password", "");
        settings.put("hibernate.hbm2ddl.auto", "create");
        settings.put("show_sql", "true");
 
        String file="export.sql";
        try {
        	 MetadataSources metadata = new MetadataSources(
                     new StandardServiceRegistryBuilder()
                             .applySettings(settings)
                             .build());

       //     new Reflections("ch.abc.mapping").getTypesAnnotatedWith(Entity.class).forEach(metadata::addAnnotatedClass);

            EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.SCRIPT);

            new File(file).delete();

            SchemaExport export = new SchemaExport();
            export.setDelimiter(";");
            export.setFormat(true);
            export.setOutputFile(file);
            export.execute(targetTypes, SchemaExport.Action.CREATE, metadata.buildMetadata());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
