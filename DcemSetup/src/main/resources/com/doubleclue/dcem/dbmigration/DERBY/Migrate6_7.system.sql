ALTER TABLE core_ldap DROP "PORT";
ALTER TABLE core_ldap DROP "SECURE";
ALTER TABLE core_ldap ADD configJson VARCHAR(4096);
ALTER TABLE as_cloudsafe ADD dc_is_folder BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE as_cloudsafe ADD dc_parent_id INTEGER NOT NULL DEFAULT 0;

DROP INDEX UK_AS_CLOUDDATA;
CREATE UNIQUE INDEX UK_AS_CLOUDDATA ON as_cloudsafe (dc_name, owner, user_dc_id, device_dc_id, dc_parent_id);

create table core_quarter_billing (
   dc_id integer not null,
    dc_data varchar(1024) not null,
    dc_timestamp date not null,
    primary key (dc_id)
);
create index quarterBillingTimestamp on core_quarter_billing (dc_timestamp);

ALTER TABLE as_cloudsafe ADD COLUMN lastModifiedUser_dc_id INTEGER;
ALTER TABLE as_cloudsafe ADD CONSTRAINT FK_AS_PROP_USER_MODIFIED FOREIGN KEY (lastModifiedUser_dc_id) REFERENCES core_user (dc_id);

create table core_alert_message (
	dc_id integer not null,
	dc_alert_category integer not null,
	dc_alert_severity integer not null,
	dc_closed timestamp,
	dc_alert_error_code integer,
	dc_message varchar(1024),
	dc_module varchar(1024) not null,
	dc_timestamp timestamp not null,
	dc_title varchar(1024) not null,
	primary key (dc_id)
);

ALTER TABLE sys_node DROP nodeType;