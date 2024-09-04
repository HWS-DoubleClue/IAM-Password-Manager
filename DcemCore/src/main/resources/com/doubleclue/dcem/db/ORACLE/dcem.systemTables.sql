
create table core_action (
dc_id number(10,0) not null,
action varchar2(128 char) not null,
moduleId varchar2(64 char) not null,
subject varchar2(128 char) not null,
primary key (dc_id)
);

create table core_auditing (
dc_id number(10,0) not null,
details clob,
auditTimeStamp timestamp,
actionId number(10,0),
audituserId number(10,0),
primary key (dc_id)
);

create table core_branch_location (
dc_id number(10,0) generated as identity,
dc_city varchar2(255 char) not null,
dc_country varchar2(255 char) not null,
dc_state varchar2(255 char),
dc_street varchar2(255 char),
dc_street_nr varchar2(32 char),
dc_zipcode varchar2(32 char),
primary key (dc_id)
);

create table core_config (
dc_id number(10,0) not null,
dc_key varchar2(128 char) not null,
moduleId varchar2(64 char),
nodeId varchar2(64 char),
dc_value long raw not null,
primary key (dc_id)
);

create table core_department (
dc_id number(19,0) generated as identity,
abbriviation varchar2(255 char),
dc_desc varchar2(255 char),
dc_name varchar2(255 char) not null,
deputy_dc_id number(10,0),
headOf_dc_id number(10,0),
dc_parent_id number(19,0),
primary key (dc_id)
);

create table core_group (
dc_id number(10,0) not null,
jpaVersion number(10,0) not null,
description varchar2(255 char),
groupDn varchar2(255 char),
dc_name varchar2(255 char) not null,
dc_role number(10,0),
dc_ldap number(10,0),
primary key (dc_id)
);

create table core_ldap (
dc_id number(10,0) not null,
baseDN varchar2(255 char) not null,
configJson long,
domainType number(10,0) not null,
enable number(1,0) not null,
filter varchar2(255 char) not null,
firstNameAttribute varchar2(255 char) not null,
host varchar2(255 char) not null,
lastNameAttribute varchar2(255 char) not null,
loginAttribute varchar2(255 char) not null,
mailAttribute varchar2(255 char),
mapEmailDomains varchar2(255 char),
mobileAttribute varchar2(255 char),
name varchar2(64 char) not null,
password long raw not null,
dc_rank number(10,0),
searchAccount varchar2(255 char) not null,
telephoneAttribute varchar2(255 char),
dc_version number(10,0),
primary key (dc_id)
);

create table core_ref_user_group (
group_id number(10,0) not null,
user_id number(10,0) not null
);

create table core_reporting (
dc_id number(19,0) not null,
action number(10,0),
errorCode varchar2(255 char),
info varchar2(255 char),
dc_time timestamp not null,
dc_loc varchar2(255 char),
severity number(10,0) not null,
show_on_dashboard number(1,0) not null,
dc_source varchar2(255 char),
user_dc_id number(10,0),
primary key (dc_id)
);

create table core_role (
dc_id number(10,0) not null,
disabled number(1,0) not null,
jpaVersion number(10,0) not null,
dc_name varchar2(64 char) not null,
dc_rank number(10,0) not null,
systemRole number(1,0) not null,
primary key (dc_id)
);

create table core_role_core_action (
core_role_dc_id number(10,0) not null,
actions_dc_id number(10,0) not null,
primary key (core_role_dc_id, actions_dc_id)
);

create table core_rolerestriction (
dc_id number(10,0) not null,
filterItem varchar2(1024 char),
jpaVersion number(10,0) not null,
moduleId varchar2(255 char),
variableName varchar2(255 char),
viewName varchar2(255 char),
dc_role number(10,0) not null,
primary key (dc_id)
);

create table core_seq (
seq_name varchar2(255 char) not null,
seq_value number(19,0),
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
dc_id number(10,0) not null,
dc_data clob not null,
dc_timestamp timestamp not null,
nodeId number(10,0),
primary key (dc_id)
);

create table core_template (
dc_id number(10,0) not null,
active number(1,0) not null,
content clob,
defaultTemplate number(1,0) not null,
inUse number(1,0) not null,
jpaVersion number(10,0) not null,
language number(10,0) not null,
lastModified timestamp,
macDigest blob,
dc_name varchar2(128 char) not null,
dc_tokens long,
dc_version number(10,0),
primary key (dc_id)
);

create table core_textMessage (
dc_id number(10,0) not null,
jpaVersion number(10,0) not null,
dc_key varchar2(255 char),
dc_value long,
textResourceBundle number(10,0),
primary key (dc_id)
);

create table core_textResourceBundle (
dc_id number(10,0) not null,
basename varchar2(255 char),
jpaVersion number(10,0) not null,
locale varchar2(255 char),
primary key (dc_id)
);

create table core_url_token (
urlToken varchar2(255 char) not null,
expiryDate timestamp not null,
objectIdentifier varchar2(255 char),
urlTokenType number(10,0) not null,
primary key (urlToken)
);

create table core_user (
dc_id number(10,0) not null,
acSuspendedTill timestamp,
disabled number(1,0) not null,
displayName varchar2(255 char),
email varchar2(255 char),
failActivations number(10,0) not null,
hashPassword long raw,
hmac blob not null,
jpaVersion number(10,0) not null,
locale number(10,0),
lastLogin timestamp,
loginId varchar2(255 char) not null,
mobileNumber varchar2(255 char),
objectGuid blob,
passCounter number(10,0) not null,
privateEmail varchar2(255 char),
prvMobile varchar2(32 char),
dc_salt blob,
saveit long raw,
dc_tel varchar2(255 char),
userDn varchar2(255 char),
userPrincipalName varchar2(255 char),
dc_role number(10,0) not null,
userext number(10,0),
dc_ldap number(10,0),
primary key (dc_id)
);

create table core_userext (
dc_userext_id number(10,0) not null,
dc_country varchar2(255 char),
jobTitle varchar2(128 char),
photo blob,
dc_timezone varchar2(255 char),
departmentid number(19,0),
primary key (dc_userext_id)
);

create table sys_dbversion (
moduleId varchar2(255 char) not null,
dbversion number(10,0),
versionStr varchar2(64 char),
primary key (moduleId)
);

create table sys_keystore (
dc_id number(10,0) not null,
cn varchar2(255 char),
disabled number(1,0) not null,
expiresOn timestamp,
ipAddress varchar2(255 char),
keyStore blob,
password long raw,
purpose number(10,0),
dc_node number(10,0),
primary key (dc_id)
);

create table sys_node (
dc_id number(10,0) not null,
dc_name varchar2(64 char) not null,
state number(10,0) not null,
wentDownOn timestamp,
primary key (dc_id)
);

create table sys_tenant (
dc_id number(10,0) not null,
dc_disabled number(1,0),
dc_fullname varchar2(255 char),
dc_master number(1,0),
dc_name varchar2(32 char) not null,
dc_schema varchar2(32 char),
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
