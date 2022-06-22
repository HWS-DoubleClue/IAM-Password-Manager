create table core_action (
dc_id integer not null,
action varchar(128) not null,
moduleId varchar(64) not null,
subject varchar(128) not null,
primary key (dc_id)
) engine=InnoDB;

create table core_auditing (
dc_id integer not null,
details longtext,
auditTimeStamp datetime,
actionId integer,
audituserId integer,
primary key (dc_id)
) engine=InnoDB;

create table core_config (
dc_id integer not null,
dc_key varchar(128) not null,
moduleId varchar(64),
nodeId varchar(64),
dc_value mediumblob not null,
primary key (dc_id)
) engine=InnoDB;

create table core_group (
dc_id integer not null,
jpaVersion integer not null,
description varchar(255),
groupDn varchar(255),
dc_name varchar(255) not null,
dc_role integer,
dc_ldap integer,
primary key (dc_id)
) engine=InnoDB;

create table core_ldap (
dc_id integer not null,
baseDN varchar(255) not null,
configJson varchar(4096),
domainType integer not null,
enable bit not null,
filter varchar(255) not null,
firstNameAttribute varchar(255) not null,
host varchar(255) not null,
lastNameAttribute varchar(255) not null,
loginAttribute varchar(255) not null,
mailAttribute varchar(255),
mapEmailDomains varchar(255),
mobileAttribute varchar(255),
name varchar(64) not null,
password mediumblob not null,
dc_rank integer,
searchAccount varchar(255) not null,
telephoneAttribute varchar(255),
dc_version integer,
primary key (dc_id)
) engine=InnoDB;

create table core_ref_user_group (
group_id integer not null,
user_id integer not null
) engine=InnoDB;

create table core_reporting (
dc_id bigint not null,
action integer,
errorCode varchar(255),
info varchar(255),
dc_loc varchar(255),
severity integer not null,
show_on_dashboard bit not null,
dc_source varchar(255),
dc_time datetime not null,
user_dc_id integer,
primary key (dc_id)
) engine=InnoDB;

create table core_role (
dc_id integer not null,
disabled bit not null,
jpaVersion integer not null,
dc_name varchar(64) not null,
dc_rank integer not null,
systemRole bit not null,
primary key (dc_id)
) engine=InnoDB;

create table core_role_core_action (
core_role_dc_id integer not null,
actions_dc_id integer not null,
primary key (core_role_dc_id, actions_dc_id)
) engine=InnoDB;

create table core_rolerestriction (
dc_id integer not null,
filterItem varchar(1024),
jpaVersion integer not null,
moduleId varchar(255),
variableName varchar(255),
viewName varchar(255),
dc_role integer not null,
primary key (dc_id)
) engine=InnoDB;

create table core_seq (
seq_name varchar(255) not null,
seq_value bigint,
primary key (seq_name)
) engine=InnoDB;

insert into core_seq(seq_name, seq_value) values ('RESOURCE_MESSAGE',1);

insert into core_seq(seq_name, seq_value) values ('CORE_GROUP.ID',1);

insert into core_seq(seq_name, seq_value) values ('ROLERESTRICTION.ID',1);

insert into core_seq(seq_name, seq_value) values ('KEYSTORE.ID',1);

insert into core_seq(seq_name, seq_value) values ('SEM_STATISTIC.ID',1);

insert into core_seq(seq_name, seq_value) values ('NODE.ID',1);

insert into core_seq(seq_name, seq_value) values ('APP_TEMPLATE_ID',1);

insert into core_seq(seq_name, seq_value) values ('ROLE.ID',1);

insert into core_seq(seq_name, seq_value) values ('LDAP.ID',1);

insert into core_seq(seq_name, seq_value) values ('SEM_ACTION.ID',1);

insert into core_seq(seq_name, seq_value) values ('SEM_CONFIG.ID',1);

insert into core_seq(seq_name, seq_value) values ('TEXT_RESOURCE',1);

insert into core_seq(seq_name, seq_value) values ('AUDIT.ID',1);

insert into core_seq(seq_name, seq_value) values ('CORE_USER.ID',1);

insert into core_seq(seq_name, seq_value) values ('TENANT.ID',1);

create table core_statistic (
dc_id integer not null,
dc_data longtext not null,
dc_timestamp datetime not null,
nodeId integer,
primary key (dc_id)
) engine=InnoDB;

create table core_template (
dc_id integer not null,
active bit not null,
content longtext,
defaultTemplate bit not null,
inUse bit not null,
jpaVersion integer not null,
language integer not null,
lastModified datetime,
macDigest tinyblob,
dc_name varchar(128) not null,
dc_tokens varchar(4096),
dc_version integer,
primary key (dc_id)
) engine=InnoDB;

create table core_textMessage (
dc_id integer not null,
jpaVersion integer not null,
dc_key varchar(255),
dc_value varchar(4096),
textResourceBundle integer,
primary key (dc_id)
) engine=InnoDB;

create table core_textResourceBundle (
dc_id integer not null,
basename varchar(255),
jpaVersion integer not null,
locale varchar(255),
primary key (dc_id)
) engine=InnoDB;

create table core_url_token (
urlToken varchar(255) not null,
expiryDate datetime not null,
objectIdentifier varchar(255),
urlTokenType integer not null,
primary key (urlToken)
) engine=InnoDB;

create table core_user (
dc_id integer not null,
acSuspendedTill datetime,
disabled bit not null,
displayName varchar(255),
email varchar(255),
failActivations integer not null,
hashPassword mediumblob,
hmac tinyblob not null,
jpaVersion integer not null,
locale integer,
lastLogin datetime,
loginId varchar(255) not null,
mobileNumber varchar(255),
objectGuid tinyblob,
passCounter integer not null,
privateEmail varchar(255),
prvMobile varchar(32),
dc_salt tinyblob,
saveit mediumblob,
dc_tel varchar(255),
userDn varchar(255),
userPrincipalName varchar(255),
dc_role integer not null,
userext integer,
dc_ldap integer,
primary key (dc_id)
) engine=InnoDB;

create table core_userext (
dc_userext_id integer not null,
dc_country varchar(255),
photo blob,
dc_timezone varchar(255),
primary key (dc_userext_id)
) engine=InnoDB;

create table sys_dbversion (
moduleId varchar(255) not null,
dbversion integer,
versionStr varchar(64),
primary key (moduleId)
) engine=InnoDB;

create table sys_keystore (
dc_id integer not null,
cn varchar(255),
disabled bit not null,
expiresOn datetime,
ipAddress varchar(255),
keyStore longblob,
password mediumblob,
purpose integer,
dc_node integer,
primary key (dc_id)
) engine=InnoDB;

create table sys_node (
dc_id integer not null,
dc_name varchar(64) not null,
startedOn datetime,
state integer not null,
wentDownOn datetime,
primary key (dc_id)
) engine=InnoDB;

create table sys_tenant (
dc_id integer not null,
dc_disabled bit,
dc_fullname varchar(255),
dc_master bit,
dc_name varchar(32) not null,
dc_schema varchar(32),
primary key (dc_id)
) engine=InnoDB;

alter table core_action
add constraint UK_SEM_ACTION unique (moduleId, subject, action);

alter table core_config
add constraint UK_CONFIG_NAME unique (moduleId, dc_key);

alter table core_group
add constraint UK_APP_GROUP unique (dc_name);

alter table core_ldap
add constraint UK_LDAP_NAME unique (name);

alter table core_role
add constraint UK_ROLE_NAME unique (dc_name);

alter table core_rolerestriction
add constraint UK_ROLE_RESTRICTION unique (dc_role, moduleId, viewName, variableName);
create index statisticTimestamp on core_statistic (dc_timestamp);

alter table core_template
add constraint UK_APP_TEMPLATE unique (dc_name, language, dc_version);

alter table core_textMessage
add constraint UK_RESOURCE_MESSAGE_KEY unique (dc_key, textResourceBundle);

alter table core_textResourceBundle
add constraint UK_RESOURCE_LOCALE_BASENAME unique (locale, basename);

alter table core_user
add constraint UK_APP_USER unique (loginId);

alter table sys_node
add constraint UK_NODE_NAME unique (dc_name);

alter table sys_tenant
add constraint UK_TENANT_NAME unique (dc_name);

alter table sys_tenant
add constraint UK_TENANT_SCHEMA unique (dc_schema);

alter table core_auditing
add constraint FK_AUDITING_ACTION
foreign key (actionId)
references core_action (dc_id);

alter table core_auditing
add constraint FK_AUDITING_USER
foreign key (audituserId)
references core_user (dc_id);

alter table core_group
add constraint FK_GROUP_ROLE
foreign key (dc_role)
references core_role (dc_id);

alter table core_group
add constraint FK_GROUP_LDAP
foreign key (dc_ldap)
references core_ldap (dc_id);

alter table core_ref_user_group
add constraint FK_GROUP_USER
foreign key (user_id)
references core_user (dc_id);

alter table core_ref_user_group
add constraint FK_USER_GROUP
foreign key (group_id)
references core_group (dc_id);

alter table core_reporting
add constraint FK_APP_REPORT_USER
foreign key (user_dc_id)
references core_user (dc_id);

alter table core_role_core_action
add constraint FK_ROLE_ACTION
foreign key (actions_dc_id)
references core_action (dc_id);

alter table core_role_core_action
add constraint FKm8fcladhxpesfv9gs7r0leqqg
foreign key (core_role_dc_id)
references core_role (dc_id);

alter table core_rolerestriction
add constraint FK_RESTRICTION_ROLE
foreign key (dc_role)
references core_role (dc_id);

alter table core_statistic
add constraint FK_APP_STATISTIC_NODE
foreign key (nodeId)
references sys_node (dc_id);

alter table core_textMessage
add constraint FK_RESOURCE_MESSAGE_BUNDLE
foreign key (textResourceBundle)
references core_textResourceBundle (dc_id);

alter table core_user
add constraint FK_USER_ROLE
foreign key (dc_role)
references core_role (dc_id);

alter table core_user
add constraint FK_USER_EXTENSION
foreign key (userext)
references core_userext (dc_userext_id);

alter table core_user
add constraint FK_USER_LDAP
foreign key (dc_ldap)
references core_ldap (dc_id);

alter table sys_keystore
add constraint FK_KEYSTORE_NODE
foreign key (dc_node)
references sys_node (dc_id);
