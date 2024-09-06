
create table core_action (
dc_id int not null,
action varchar(128) not null,
moduleId varchar(64) not null,
subject varchar(128) not null,
primary key (dc_id)
);

create table core_auditing (
dc_id int not null,
details varchar(MAX),
auditTimeStamp datetime2,
actionId int,
audituserId int,
primary key (dc_id)
);

create table core_branch_location (
dc_id int identity not null,
dc_city varchar(255) not null,
dc_country varchar(255) not null,
dc_state varchar(255),
dc_street varchar(255),
dc_street_nr varchar(32),
dc_zipcode varchar(32),
primary key (dc_id)
);

create table core_config (
dc_id int not null,
dc_key varchar(128) not null,
moduleId varchar(64),
nodeId varchar(64),
dc_value varbinary(MAX) not null,
primary key (dc_id)
);

create table core_department (
dc_id bigint identity not null,
abbriviation varchar(255),
dc_desc varchar(255),
dc_name varchar(255) not null,
deputy_dc_id int,
headOf_dc_id int,
dc_parent_id bigint,
primary key (dc_id)
);

create table core_group (
dc_id int not null,
jpaVersion int not null,
description varchar(255),
groupDn varchar(255),
dc_name varchar(255) not null,
dc_role int,
dc_ldap int,
primary key (dc_id)
);

create table core_ldap (
dc_id int not null,
baseDN varchar(255) not null,
configJson varchar(4096),
domainType int not null,
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
password varbinary(MAX) not null,
dc_rank int,
searchAccount varchar(255) not null,
telephoneAttribute varchar(255),
dc_version int,
primary key (dc_id)
);

create table core_ref_user_group (
group_id int not null,
user_id int not null
);

create table core_reporting (
dc_id bigint not null,
action int,
errorCode varchar(255),
info varchar(255),
dc_time datetime2 not null,
dc_loc varchar(255),
severity int not null,
show_on_dashboard bit not null,
dc_source varchar(255),
user_dc_id int,
primary key (dc_id)
);

create table core_role (
dc_id int not null,
disabled bit not null,
jpaVersion int not null,
dc_name varchar(64) not null,
dc_rank int not null,
systemRole bit not null,
primary key (dc_id)
);

create table core_role_core_action (
core_role_dc_id int not null,
actions_dc_id int not null,
primary key (core_role_dc_id, actions_dc_id)
);

create table core_rolerestriction (
dc_id int not null,
filterItem varchar(1024),
jpaVersion int not null,
moduleId varchar(255),
variableName varchar(255),
viewName varchar(255),
dc_role int not null,
primary key (dc_id)
);

create table core_seq (
seq_name varchar(255) not null,
seq_value bigint,
primary key (seq_name)
);

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
dc_id int not null,
dc_data varchar(MAX) not null,
dc_timestamp datetime2 not null,
nodeId int,
primary key (dc_id)
);

create table core_template (
dc_id int not null,
active bit not null,
content varchar(MAX),
defaultTemplate bit not null,
inUse bit not null,
jpaVersion int not null,
language int not null,
lastModified datetime2,
macDigest varbinary(32),
dc_name varchar(128) not null,
dc_tokens varchar(4096),
dc_version int,
primary key (dc_id)
);

create table core_textMessage (
dc_id int not null,
jpaVersion int not null,
dc_key varchar(255),
dc_value varchar(4096),
textResourceBundle int,
primary key (dc_id)
);

create table core_textResourceBundle (
dc_id int not null,
basename varchar(255),
jpaVersion int not null,
locale varchar(255),
primary key (dc_id)
);

create table core_url_token (
urlToken varchar(255) not null,
expiryDate datetime2 not null,
objectIdentifier varchar(255),
urlTokenType int not null,
primary key (urlToken)
);

create table core_user (
dc_id int not null,
acSuspendedTill datetime2,
disabled bit not null,
displayName varchar(255),
email varchar(255),
failActivations int not null,
hashPassword varbinary(MAX),
hmac varbinary(32) not null,
jpaVersion int not null,
locale int,
lastLogin datetime2,
loginId varchar(255) not null,
mobileNumber varchar(255),
objectGuid varbinary(255),
passCounter int not null,
privateEmail varchar(255),
prvMobile varchar(32),
dc_salt varbinary(32),
saveit varbinary(MAX),
dc_tel varchar(255),
userDn varchar(255),
userPrincipalName varchar(255),
dc_role int not null,
userext int,
dc_ldap int,
primary key (dc_id)
);

create table core_userext (
dc_userext_id int not null,
dc_country varchar(255),
jobTitle varchar(128),
photo varbinary(MAX),
dc_timezone varchar(255),
departmentid bigint,
primary key (dc_userext_id)
);

create table sys_dbversion (
moduleId varchar(255) not null,
dbversion int,
versionStr varchar(64),
primary key (moduleId)
);

create table sys_keystore (
dc_id int not null,
cn varchar(255),
disabled bit not null,
expiresOn datetime2,
ipAddress varchar(255),
keyStore varbinary(MAX),
password varbinary(MAX),
purpose int,
dc_node int,
primary key (dc_id)
);

create table sys_node (
dc_id int not null,
dc_name varchar(64) not null,
state int not null,
wentDownOn datetime2,
primary key (dc_id)
);

create table sys_tenant (
dc_id int not null,
dc_disabled bit,
dc_fullname varchar(255),
dc_master bit,
dc_name varchar(32) not null,
dc_schema varchar(32),
primary key (dc_id)
);

alter table core_action
add constraint UK_SEM_ACTION unique (moduleId, subject, action);

alter table core_config
add constraint UK_CONFIG_NAME unique (moduleId, dc_key);

alter table core_department
add constraint UK_DEPARTMENT_NAME unique (dc_name);

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
references core_action;

alter table core_auditing
add constraint FK_AUDITING_USER
foreign key (audituserId)
references core_user;

alter table core_department
add constraint FK_APP_DEPARTMENT_USER_DEPUTY
foreign key (deputy_dc_id)
references core_user;

alter table core_department
add constraint FK_APP_DEPARTMENT_USER
foreign key (headOf_dc_id)
references core_user;

alter table core_department
add constraint FK_DEPARTMENT_PARENT_ID
foreign key (dc_parent_id)
references core_department;

alter table core_group
add constraint FK_GROUP_ROLE
foreign key (dc_role)
references core_role;

alter table core_group
add constraint FK_GROUP_LDAP
foreign key (dc_ldap)
references core_ldap;

alter table core_ref_user_group
add constraint FK_GROUP_USER
foreign key (user_id)
references core_user;

alter table core_ref_user_group
add constraint FK_USER_GROUP
foreign key (group_id)
references core_group;

alter table core_reporting
add constraint FK_APP_REPORT_USER
foreign key (user_dc_id)
references core_user;

alter table core_role_core_action
add constraint FK_ROLE_ACTION
foreign key (actions_dc_id)
references core_action;

alter table core_role_core_action
add constraint FKm8fcladhxpesfv9gs7r0leqqg
foreign key (core_role_dc_id)
references core_role;

alter table core_rolerestriction
add constraint FK_RESTRICTION_ROLE
foreign key (dc_role)
references core_role;

alter table core_statistic
add constraint FK_APP_STATISTIC_NODE
foreign key (nodeId)
references sys_node;

alter table core_textMessage
add constraint FK_RESOURCE_MESSAGE_BUNDLE
foreign key (textResourceBundle)
references core_textResourceBundle;

alter table core_user
add constraint FK_USER_ROLE
foreign key (dc_role)
references core_role;

alter table core_user
add constraint FK_USER_EXTENSION
foreign key (userext)
references core_userext;

alter table core_user
add constraint FK_USER_LDAP
foreign key (dc_ldap)
references core_ldap;

alter table core_userext
add constraint FK_DEPARTMENT_USEREXT_ID
foreign key (departmentid)
references core_department;

alter table sys_keystore
add constraint FK_KEYSTORE_NODE
foreign key (dc_node)
references sys_node;
