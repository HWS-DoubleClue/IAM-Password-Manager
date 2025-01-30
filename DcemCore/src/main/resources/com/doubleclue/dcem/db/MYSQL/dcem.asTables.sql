
create table as_activationcode (
dc_id integer not null,
activationCode mediumblob not null,
createdOn datetime not null,
info varchar(255),
validTill datetime not null,
userId integer not null,
primary key (dc_id)
) engine=InnoDB;

create table as_app_policy_group (
dc_id integer not null,
dc_priority integer,
group_id integer,
policyApp_id integer not null,
policy_id integer,
primary key (dc_id)
) engine=InnoDB;

create table as_authApp (
dc_id integer not null,
disabled bit not null,
dc_name varchar(64),
retryCounter integer not null,
sharedKey mediumblob,
primary key (dc_id)
) engine=InnoDB;

create table as_cloudsafe (
dc_id integer not null,
dcemMediaType integer,
discardAfter datetime,
dc_info varchar(255),
dc_is_folder bit not null,
dc_gcm bit not null,
lastModified datetime,
dc_length bigint,
dc_name varchar(255) not null,
options varchar(255),
owner integer,
recycled bit not null,
dc_salt tinyblob,
textExtract mediumblob,
text_length bigint,
device_dc_id integer not null,
group_dc_id integer,
lastModifiedUser_dc_id integer,
dc_parent_id integer,
user_dc_id integer not null,
primary key (dc_id)
) engine=InnoDB;

create table as_cloudsafe_tag (
dc_id integer not null auto_increment,
dc_color varchar(64) not null,
dc_name varchar(255) not null,
primary key (dc_id)
) engine=InnoDB;

create table as_cloudsafecontent (
cloudDataEntity_dc_id integer not null,
content longblob,
primary key (cloudDataEntity_dc_id)
) engine=InnoDB;

create table as_cloudsafelimit (
expiry_date datetime,
dc_limit bigint not null,
ps_enabled bit not null,
dc_used bigint not null,
user_dc_id integer not null,
primary key (user_dc_id)
) engine=InnoDB;

create table as_cloudsafeshare (
dc_id integer not null,
restrictDownload bit,
writeAccess bit,
cloudSafe_dc_id integer,
group_dc_id integer,
user_dc_id integer,
primary key (dc_id)
) engine=InnoDB;

create table as_cloudsafethumbnail (
dc_id integer not null,
thumbnail longblob,
primary key (dc_id)
) engine=InnoDB;

create table as_device (
dc_id integer not null,
appOsVersion varchar(255),
deviceHash tinyblob,
deviceKey mediumblob,
lastLogin datetime,
locale varchar(2),
manufacture varchar(255),
name varchar(64),
nodeId integer,
offlineCounter integer not null,
offlineKey mediumblob,
publicKey blob,
retryCounter integer not null,
risks varchar(255),
dc_state integer not null,
dc_status integer not null,
udid tinyblob,
asVersion_dc_id integer,
userId integer not null,
primary key (dc_id)
) engine=InnoDB;

create table as_fido_authenticator (
dc_id integer not null,
credentialId varchar(255) not null,
display_name varchar(255) not null,
lastUsed datetime not null,
passwordless bit not null,
publicKey blob not null,
registeredOn datetime not null,
userId integer not null,
primary key (dc_id)
) engine=InnoDB;

create table as_message (
dc_id bigint not null,
actionId varchar(64),
createdOn datetime not null,
info varchar(255),
dc_status integer not null,
outputData varchar(4096),
responseData varchar(4096),
responseRequired bit not null,
retrieved bit not null,
signature tinyblob,
signed bit not null,
device_dc_id integer,
operatorId integer,
policyAppId integer,
template_dc_id integer,
userId integer not null,
primary key (dc_id)
) engine=InnoDB;

create table as_policy (
dc_id integer not null,
jsonPolicy varchar(4096),
dc_name varchar(255),
primary key (dc_id)
) engine=InnoDB;

create table as_policy_app (
dc_id integer not null,
authapp varchar(255),
dc_disabled bit,
subId integer not null,
subname varchar(255),
primary key (dc_id)
) engine=InnoDB;

create table as_ref_cloudsafe_tag (
dc_id integer not null,
tags_dc_id integer not null,
primary key (dc_id, tags_dc_id)
) engine=InnoDB;

create table as_userfingerprint (
policyAppId integer not null,
userId integer not null,
fingerprint varchar(255),
timeStamp datetime,
primary key (policyAppId, userId)
) engine=InnoDB;

create table as_version (
dc_id integer not null,
clientType integer,
dc_disabled bit,
downloadUrl varchar(255),
expiresOn datetime,
informationUrl varchar(255),
jpaVersion integer not null,
dc_name varchar(128),
testApp bit,
as_version integer,
versionStr varchar(128),
user_dc_id integer,
primary key (dc_id)
) engine=InnoDB;

alter table as_authApp
add constraint UK_AUTHAPP_NAME unique (dc_name);

alter table as_cloudsafe
add constraint UK_AS_CLOUDDATA unique (dc_name, owner, user_dc_id, device_dc_id, dc_parent_id, group_dc_id, recycled);

alter table as_cloudsafe_tag
add constraint UK_DM_TAG_NAME unique (dc_name);

create index IDX_DEVICE_LAST_LOGIN on as_device (lastLogin, dc_state);

create index IDX_DEVICE_USER on as_device (userId);

alter table as_device
add constraint UK_DEVICE_USER unique (userId, name);

create index FIDO_AUTH_CREDENTIAL_ID_INDEX on as_fido_authenticator (credentialId);

alter table as_fido_authenticator
add constraint UK_USER_CREDENTIAL_ID unique (userId, credentialId);

alter table as_policy
add constraint UK_POLICY_NAME unique (dc_name);

alter table as_policy_app
add constraint UK_POLICY_APP unique (authapp, subId);

alter table as_version
add constraint UK_VERSION_NAME_TYPE unique (dc_name, versionStr, clientType);

alter table as_activationcode
add constraint FK_APP_AC_USER
foreign key (userId)
references core_user (dc_id);

alter table as_app_policy_group
add constraint FK_REF_GROUP
foreign key (group_id)
references core_group (dc_id);

alter table as_app_policy_group
add constraint FK_REF_APP_POLICY
foreign key (policyApp_id)
references as_policy_app (dc_id);

alter table as_app_policy_group
add constraint FK_REF_POLICY
foreign key (policy_id)
references as_policy (dc_id);

alter table as_cloudsafe
add constraint FK_AS_PROP_DEVICE
foreign key (device_dc_id)
references as_device (dc_id);

alter table as_cloudsafe
add constraint FK_AS_PROP_GROUP
foreign key (group_dc_id)
references core_group (dc_id);

alter table as_cloudsafe
add constraint FK_AS_PROP_USER_MODIFIED
foreign key (lastModifiedUser_dc_id)
references core_user (dc_id);

alter table as_cloudsafe
add constraint FK_AS_PARENT_ID
foreign key (dc_parent_id)
references as_cloudsafe (dc_id);

alter table as_cloudsafe
add constraint FK_AS_PROP_USER
foreign key (user_dc_id)
references core_user (dc_id);

alter table as_cloudsafelimit
add constraint FK_CLOUDSAFE_LIMIT
foreign key (user_dc_id)
references core_user (dc_id);

alter table as_cloudsafeshare
add constraint FK_AS_CD_SHARE
foreign key (cloudSafe_dc_id)
references as_cloudsafe (dc_id)
on delete cascade;

alter table as_cloudsafeshare
add constraint FK_AS_CD_GROUP
foreign key (group_dc_id)
references core_group (dc_id);

alter table as_cloudsafeshare
add constraint FK_AS_CD_SHARE_USER
foreign key (user_dc_id)
references core_user (dc_id);

alter table as_cloudsafethumbnail
add constraint FK_CLOUDSAFE_THUMBNAIL
foreign key (dc_id)
references as_cloudsafe (dc_id);

alter table as_device
add constraint FK_APP_DEVICE_VERSION
foreign key (asVersion_dc_id)
references as_version (dc_id);

alter table as_device
add constraint FK_APP_DEVICE_USER
foreign key (userId)
references core_user (dc_id);

alter table as_fido_authenticator
add constraint FK_FIDO_USER
foreign key (userId)
references core_user (dc_id);

alter table as_message
add constraint FK_APP_MSG_DEVICE
foreign key (device_dc_id)
references as_device (dc_id);

alter table as_message
add constraint FK_MSG_OPERATOR
foreign key (operatorId)
references core_user (dc_id);

alter table as_message
add constraint FK_MSG_POLICYAPP
foreign key (policyAppId)
references as_policy_app (dc_id);

alter table as_message
add constraint FK_APP_MSG_TEMPLATE
foreign key (template_dc_id)
references core_template (dc_id);

alter table as_message
add constraint FK_APP_MSG_USER
foreign key (userId)
references core_user (dc_id);

alter table as_ref_cloudsafe_tag
add constraint FKtn5egj0ktr5del1n3rrrrvq3a
foreign key (tags_dc_id)
references as_cloudsafe_tag (dc_id);

alter table as_ref_cloudsafe_tag
add constraint FK_CLOUDSAFE_TAG
foreign key (dc_id)
references as_cloudsafe (dc_id);

alter table as_version
add constraint FK_APP_VERSION_USER
foreign key (user_dc_id)
references core_user (dc_id);
