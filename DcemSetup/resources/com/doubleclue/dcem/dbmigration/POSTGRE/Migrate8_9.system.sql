ALTER TABLE as_reporting ADD COLUMN dc_source VARCHAR(255);
ALTER TABLE as_reporting ADD COLUMN severity INTEGER NOT NULL DEFAULT 0;
ALTER TABLE as_reporting ADD COLUMN show_on_dashboard BOOLEAN DEFAULT false NOT NULL;
ALTER TABLE as_version ADD COLUMN testApp BOOLEAN DEFAULT false NOT NULL;

UPDATE as_reporting AS r
	SET dc_source = subname
	FROM as_policy_app AS pa
	WHERE r.application_dc_id IS NOT NULL
		AND r.application_dc_id = pa.dc_id;

UPDATE as_reporting AS r
	SET dc_source = authapp
	FROM as_policy_app AS pa
	WHERE r.application_dc_id IS NOT NULL
		AND r.dc_source IS NULL
		AND r.application_dc_id = pa.dc_id;

UPDATE as_reporting
	SET severity = 1
	WHERE errorCode IS NOT NULL;

ALTER TABLE as_reporting DROP CONSTRAINT FK_APP_REPORTING_APPLICATION;
ALTER TABLE as_reporting DROP COLUMN application_dc_id;
ALTER TABLE as_reporting RENAME TO core_reporting;

DROP TABLE core_alert_message;

create sequence up_apphubseq start with 1 increment by  4;

create table up_applicationhub (
up_id int4 not null,
application varchar(10000) not null,
included boolean not null,
logo bytea,
up_name varchar(255) not null,
primary key (up_id)
);

create table up_applicationhubdashboard (
up_application int4 not null,
up_user int4 not null,
up_index int4,
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

ALTER TABLE as_cloudsafe ALTER COLUMN dc_parent_id DROP NOT NULL;
ALTER TABLE as_cloudsafe ALTER COLUMN dc_parent_id SET DEFAULT NULL;

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

ALTER TABLE as_cloudsafe ADD COLUMN dc_gcm boolean DEFAULT false NOT NULL;
ALTER TABLE as_cloudsafe DROP COLUMN dc_signature;  
ALTER TABLE as_cloudsafe DROP COLUMN sign;  

ALTER TABLE as_cloudsafe ADD COLUMN recycled boolean DEFAULT false NOT NULL;

ALTER TABLE as_device ALTER COLUMN asVersion_dc_id DROP NOT NULL;
ALTER TABLE as_device ALTER COLUMN asVersion_dc_id SET DEFAULT NULL;