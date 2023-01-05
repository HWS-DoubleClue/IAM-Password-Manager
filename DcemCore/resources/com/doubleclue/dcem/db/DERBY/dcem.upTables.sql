
create table up_applicationhub (
up_id integer not null,
application varchar(10000) not null,
logo varchar(32000) for bit data,
up_name varchar(255) not null,
primary key (up_id)
);

create table up_keepassentry (
dc_id varchar(255) not null,
application varchar(10000),
up_name varchar(255) not null,
appEntity integer,
primary key (dc_id)
);
create unique index UK_SEM_ACTION on core_action (moduleId, subject, action);
create unique index UK_APP_GROUP on core_group (dc_name);
create unique index UK_LDAP_NAME on core_ldap (name);
create unique index UK_ROLE_NAME on core_role (dc_name);
create unique index UK_APP_USER on core_user (loginId);
create unique index UK_APPHUB_NAME on up_applicationhub (up_name);

alter table up_keepassentry
add constraint FK_KEEPASS_APP
foreign key (appEntity)
references up_applicationhub;
