ALTER TABLE as_reporting ADD dc_source VARCHAR(255);
ALTER TABLE as_reporting ADD severity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE as_reporting ADD show_on_dashboard BIT NOT NULL DEFAULT 0;

if not exists (select
                     'testapp'
               from
                     INFORMATION_SCHEMA.columns
               where
                     table_name = 'as_version'
                     and column_name = 'testapp')
ALTER TABLE as_version ADD  testApp BIT NOT NULL DEFAULT 0;

UPDATE as_reporting 
SET dc_source = pa.subname
FROM as_policy_app pa
WHERE application_dc_id IS NOT NULL
	AND application_dc_id = pa.dc_id;

UPDATE as_reporting 
SET dc_source = pa.authapp
FROM as_policy_app pa
WHERE application_dc_id IS NOT NULL
	AND dc_source IS NULL
	AND application_dc_id = pa.dc_id;

UPDATE as_reporting 
SET severity = 1
WHERE errorCode IS NOT NULL;

ALTER TABLE as_reporting DROP CONSTRAINT FK_APP_REPORTING_APPLICATION;
ALTER TABLE as_reporting DROP COLUMN application_dc_id;
EXEC sp_rename 'as_reporting', 'core_reporting';

DROP TABLE core_alert_message;

create table up_apphubseq (
next_val bigint
);

insert into up_apphubseq values ( 1 );

create table up_applicationhub (
up_id int not null,
application varchar(MAX) not null,
included bit not null,
logo varbinary(MAX),
up_name varchar(255) not null,
primary key (up_id)
);

create table up_applicationhubdashboard (
up_application int not null,
up_user int not null,
up_index int,
primary key (up_application, up_user)
);

alter table up_applicationhub add constraint UK_APPHUB_NAME unique (up_name);

ALTER TABLE as_cloudsafecontent DROP CONSTRAINT FK_CLOUDSAFE_CONTENT;

alter table up_applicationhubdashboard
add constraint FK_REF_APPHUB
foreign key (up_application)
references up_applicationhub;

alter table up_applicationhubdashboard
add constraint FK_REF_USER
foreign key (up_user)
references core_user;

create table up_apphub_group (
apphub_up_id integer not null,
group_dc_id integer not null,
primary key (apphub_up_id, group_dc_id)
);

alter table up_apphub_group
add constraint FK_APPHUB_GROUP
foreign key (group_dc_id)
references core_group (dc_id);

alter table up_apphub_group
add constraint FK1wkg76ssvji8w1pag3w0atcw4
foreign key (apphub_up_id)
references up_applicationhub (up_id);

ALTER TABLE as_cloudsafe
ALTER COLUMN dc_parent_id int NULL;

UPDATE as_cloudsafe 
SET dc_parent_id = NULL WHERE dc_parent_id = 0;

ALTER TABLE as_cloudsafe 
ADD CONSTRAINT FK_AS_PARENT_ID 
FOREIGN KEY (dc_parent_id) 
REFERENCES as_cloudsafe (dc_id);

CREATE TRIGGER [Delete_Parent]
   ON [dbo].[as_cloudsafe]
   INSTEAD OF DELETE
AS 
BEGIN
	SET NOCOUNT ON

    CREATE TABLE #temp(
        Id    INT
    )
    INSERT INTO #temp (Id)
    SELECT  dc_id
    FROM    DELETED

    DECLARE @c INT
    SET @c = 0

    WHILE @c <> (SELECT COUNT(Id) FROM #temp) BEGIN
        SELECT @c = COUNT(Id) FROM #temp

        INSERT INTO #temp (Id)
			SELECT  as_cloudsafe.dc_id
			FROM    as_cloudsafe
			LEFT OUTER JOIN #temp ON as_cloudsafe.dc_id = #temp.Id
			WHERE   as_cloudsafe.dc_parent_id IN (SELECT Id FROM #temp)
			AND     #temp.Id IS NULL
    END

	DELETE [as_cloudsafeshare] FROM [as_cloudsafeshare]
	INNER JOIN #temp ON [as_cloudsafeshare].cloudSafe_dc_id = #temp.Id

    DELETE  as_cloudsafe FROM as_cloudsafe
    INNER JOIN #temp ON as_cloudsafe.dc_id = #temp.Id
    
END;

ALTER TABLE as_cloudsafe ADD dc_gcm BIT NOT NULL DEFAULT 0;
ALTER TABLE as_cloudsafe DROP COLUMN dc_signature;  
ALTER TABLE as_cloudsafe DROP COLUMN sign;  
ALTER TABLE as_cloudsafe ALTER COLUMN dc_salt varbinary(32);

ALTER TABLE as_cloudsafe ADD recycled BIT NOT NULL DEFAULT 0;

ALTER TABLE as_device ALTER COLUMN asVersion_dc_id int null;
