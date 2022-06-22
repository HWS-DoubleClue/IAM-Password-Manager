ALTER TABLE as_reporting ADD dc_source VARCHAR(255);
ALTER TABLE as_reporting ADD severity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE as_reporting ADD show_on_dashboard BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE as_version ADD testApp BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE as_cloudsafe ALTER COLUMN dc_salt SET DATA TYPE VARCHAR(32) FOR BIT DATA;

UPDATE as_reporting
SET dc_source = (SELECT subname 
				FROM as_policy_app
				WHERE as_reporting.application_dc_id = as_policy_app.dc_id)
WHERE as_reporting.application_dc_id IS NOT NULL
	AND EXISTS (SELECT *
				FROM as_policy_app
				WHERE as_reporting.application_dc_id = as_policy_app.dc_id);
             
	
UPDATE as_reporting
SET dc_source = (SELECT authapp 
				FROM as_policy_app
				WHERE as_reporting.application_dc_id = as_policy_app.dc_id)
WHERE EXISTS (SELECT *
				FROM as_policy_app
				WHERE as_reporting.application_dc_id = as_policy_app.dc_id)
	AND as_reporting.application_dc_id IS NOT NULL
	AND as_reporting.dc_source IS NULL;
	

UPDATE as_reporting
	SET severity = 1
	WHERE errorCode IS NOT NULL;

ALTER TABLE as_reporting DROP CONSTRAINT FK_APP_REPORTING_APPLICATION;
ALTER TABLE as_reporting DROP "APPLICATION_DC_ID";
RENAME TABLE as_reporting TO core_reporting;

DROP TABLE core_alert_message;

create sequence up_apphubseq start with 1 increment by  4;

create table up_applicationhub (
up_id integer not null,
application varchar(10000) not null,
included boolean not null,
logo varchar(32600) for bit data,
up_name varchar(255) not null,
primary key (up_id)
);

create table up_applicationhubdashboard (
up_application integer not null,
up_user integer not null,
up_index integer,
primary key (up_application, up_user)
);

alter table up_applicationhub add constraint UK_APPHUB_NAME unique (up_name);

ALTER TABLE as_cloudsafecontent DROP FOREIGN KEY FK_CLOUDSAFE_CONTENT;

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
ALTER COLUMN dc_parent_id NULL;

UPDATE as_cloudsafe 
SET dc_parent_id = NULL WHERE dc_parent_id = 0;

ALTER TABLE as_cloudsafe 
ADD CONSTRAINT FK_AS_PARENT_ID 
FOREIGN KEY (dc_parent_id) 
REFERENCES as_cloudsafe (dc_id);

ALTER TABLE as_cloudsafeshare DROP CONSTRAINT FK_AS_CD_SHARE;
ALTER TABLE as_cloudsafeshare 
ADD CONSTRAINT FK_AS_CD_SHARE 
FOREIGN KEY (cloudSafe_dc_id) 
REFERENCES as_cloudsafe (dc_id) 
ON DELETE CASCADE;

ALTER TABLE as_cloudsafe ADD dc_gcm BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE as_cloudsafe DROP dc_signature;  
ALTER TABLE as_cloudsafe DROP sign; 

ALTER TABLE as_cloudsafe ADD recycled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE as_device ALTER COLUMN asVersion_dc_id NULL;
