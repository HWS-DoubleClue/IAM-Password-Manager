#Update Database
When there are some changes in the entities, the Database and the migration scripts have to be updated.

This Documentation describes the whole process.

Some steps can be skipped because they are not required.

## Entities
### New Entity
In some cases you have to create a new Entity class, e.g. when You want to add a new Table.
In this Example we use the class``UrlTokenEntity`` in the Core Module.

Entity classes must be in the following directory of the affected module:
```
com.doubleclue.dcem.core.entities
```
If the entity is in another module, you wil have to replace "core" with the module name.

After that you must add the Class to the persistence configuration of the module.

You can find this file under the following path of the Module:

````
.\DcemCore\src\META-INF\persistence.xml
````

There you have to add the following line:
````xml
<class>com.doubleclue.dcem.core.entities.UrlTokenEntity</class>
````

### Modified Entity
If you make some changes in an entity class you will also have to create migration scripts. 
But this a little bit more difficult because you must write "Update" SQL scripts.
The procedure for this is similar to the "New Entity" scenario. You have to find a SQL script which modifies the 
current database table, so the table fits to the entity after running the SQL script.

## Create Tables
To create the sql scripts for the tables there is a class which does that for all modules automatically.
You can find this class in the following directory:
```
.\DcemSetup\testsrc\CreateTables.java
```
You will need one program argument which contains the workspace path to run this 'Application'.
In Eclipse there is a predefined constant for that called ``${workspace_loc}``.

## Update Migration Scripts
This process has to be done manually because there we have no good solution which does that automatically.
But that is not so difficult.

### Storage directory of Migration Scripts
All Migration Scripts are stored in the same directoy in the DcemSetup Package.
The path is ``DcemSetup\resources\com\doubleclue\dcem\dbmigration\``. Each Database has his own directory becaues SQL scripts can be different on some databases.

The name of the scripts are assembled as follows:

Migrate***oldMigrationNumber***_***newMigrationVersion***.***ModuleName***.sql

In Our we have the following arguments:
- oldMigrationNumber: **3**
- newMigrationVersion: **4** (This is always **oldMigrationNumber** + 1)
- ModuleName: system

The the result is **`Migrate3_4.system.sql`**

Not every module has the same DB Version Number because the migration scripts are only generated when the database has changed.

**IMPORTANT**

When you have created a new migration file, you have to increase the DB version number of the module.
Each module has a class which contains some configurations. All these classes are extending the java class
``DcemModule``. You can find this class under the following path of each module with own migration scripts:
``.\DcemCore\src\com\doubleclue\dcem\system\logic``. This path is again for the specific example from the beginning.
- **DcemCore** should be replaced with the directory name of the module
- **system** should be replaced with the module name

The class is named by the following system:
<***module Name***>Module 

In this case the class is named **SystemModule**

In this class you can find a method called "getDbVersion"
````java
public int getDbVersion() {
	return 4;
}
````
This method contains the current DB Version Number (in this case '4') and has to be increased with every new migration script 
of this module. If this is not updated correctly it can produce **fatal runtime errors**!

### Create Migration Scripts
To create the migration scripts you have to look at the creation scripts an se what has been changed since the last version.
This can be a little bit tricky.
It is much easier when you update the migration scripts immediately after some changes in the Entities.

**It's important to test the scripts!!!**