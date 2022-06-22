ALTER TABLE core_url_token DROP CONSTRAINT FK_APP_URL_TOKEN_USER;
ALTER TABLE core_url_token DROP COLUMN userId;
ALTER TABLE core_url_token ADD COLUMN objectIdentifier VARCHAR(255) NULL DEFAULT NULL;

ALTER TABLE up_applicationhub DROP COLUMN included;

DROP TABLE up_applicationhubdashboard;
DROP TABLE core_billing;

    create table core_userext (
       dc_userext_id integer not null,
        dc_country varchar(255),
        photo blob,
        dc_timezone varchar(255),
        primary key (dc_userext_id)
    ) engine=InnoDB;
    
create table up_keepassentry (
dc_id varchar(255) not null,
application varchar(10000),
up_name varchar(255) not null,
appEntity integer,
primary key (dc_id)
) engine=InnoDB;

alter table up_keepassentry
add constraint FK_KEEPASS_APP
foreign key (appEntity)
references up_applicationhub (up_id);

ALTER TABLE core_user ADD COLUMN userext integer DEFAULT NULL;

alter table core_user
add constraint FK_USER_EXTENSION
foreign key (userext)
references core_userext (dc_userext_id);