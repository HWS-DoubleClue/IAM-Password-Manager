ALTER TABLE core_ldap DROP COLUMN port;
ALTER TABLE core_ldap DROP COLUMN secure;
ALTER TABLE core_ldap ADD COLUMN configJson VARCHAR(4096);
ALTER TABLE as_cloudsafe ADD COLUMN dc_is_folder BIT DEFAULT 0 NOT NULL;
ALTER TABLE as_cloudsafe ADD COLUMN dc_parent_id INTEGER NOT NULL;
ALTER TABLE as_cloudsafe DROP CONSTRAINT UK_AS_CLOUDDATA;
ALTER TABLE as_cloudsafe ADD CONSTRAINT UK_AS_CLOUDDATA unique (dc_name, owner, user_dc_id, device_dc_id, dc_parent_id);

create table core_quarter_billing (
   dc_id integer not null,
    dc_data varchar(1024) not null,
    dc_timestamp date not null,
    primary key (dc_id)
);
create index quarterBillingTimestamp on core_quarter_billing (dc_timestamp);

ALTER TABLE as_cloudsafe ADD COLUMN lastModifiedUser_dc_id INTEGER;
alter table as_cloudsafe add constraint FK_AS_PROP_USER_MODIFIED foreign key (lastModifiedUser_dc_id) references core_user (dc_id);

create table core_alert_message (
   dc_id integer not null,
    dc_alert_category integer not null,
    dc_alert_severity integer not null,
    dc_closed datetime,
    dc_alert_error_code integer,
    dc_message varchar(1024),
    dc_module varchar(1024) not null,
    dc_timestamp datetime not null,
    dc_title varchar(1024) not null,
    primary key (dc_id)
) engine=InnoDB;

ALTER TABLE sys_node DROP COLUMN nodeType;