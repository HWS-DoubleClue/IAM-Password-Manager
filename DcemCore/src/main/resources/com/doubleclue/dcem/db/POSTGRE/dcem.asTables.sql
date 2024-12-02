
create table as_activationcode (
dc_id int4 not null,
activationCode bytea not null,
createdOn timestamp not null,
info varchar(255),
validTill timestamp not null,
userId int4 not null,
primary key (dc_id)
);

create table as_app_policy_group (
dc_id int4 not null,
dc_priority int4,
group_id int4,
policyApp_id int4 not null,
policy_id int4,
primary key (dc_id)
);

create table as_authApp (
dc_id int4 not null,
disabled boolean not null,
dc_name varchar(64),
retryCounter int4 not null,
sharedKey bytea,
primary key (dc_id)
);

create table as_cloudsafe (
dc_id int4 not null,
dcemMediaType int4,
discardAfter timestamp,
dc_info varchar(255),
dc_is_folder boolean not null,
dc_gcm boolean not null,
lastModified timestamp,
dc_length int8,
dc_name varchar(255) not null,
options varchar(255),
owner int4,
recycled boolean not null,
dc_salt bytea,
textExtract bytea,
text_length int8,
device_dc_id int4 not null,
group_dc_id int4,
lastModifiedUser_dc_id int4,
dc_parent_id int4,
user_dc_id int4 not null,
primary key (dc_id)
);

create table as_cloudsafe_tag (
dc_id  serial not null,
dc_color varchar(64) not null,
dc_name varchar(255) not null,
primary key (dc_id)
);

create table as_cloudsafecontent (
cloudDataEntity_dc_id int4 not null,
content oid,
primary key (cloudDataEntity_dc_id)
);

create table as_cloudsafelimit (
expiry_date timestamp,
dc_limit int8 not null,
ps_enabled boolean not null,
dc_used int8 not null,
user_dc_id int4 not null,
primary key (user_dc_id)
);

create table as_cloudsafeshare (
dc_id int4 not null,
restrictDownload boolean,
writeAccess boolean,
cloudSafe_dc_id int4,
group_dc_id int4,
user_dc_id int4,
primary key (dc_id)
);

create table as_cloudsafethumbnail (
dc_id int4 not null,
thumbnail oid,
primary key (dc_id)
);

create table as_device (
dc_id int4 not null,
appOsVersion varchar(255),
deviceHash bytea,
deviceKey bytea,
lastLogin timestamp,
locale varchar(2),
manufacture varchar(255),
name varchar(64),
nodeId int4,
offlineCounter int4 not null,
offlineKey bytea,
publicKey bytea,
retryCounter int4 not null,
risks varchar(255),
dc_state int4 not null,
dc_status int4 not null,
udid bytea,
asVersion_dc_id int4,
userId int4 not null,
primary key (dc_id)
);

create table as_fido_authenticator (
dc_id int4 not null,
credentialId varchar(255) not null,
display_name varchar(255) not null,
lastUsed timestamp not null,
passwordless boolean not null,
publicKey bytea not null,
registeredOn timestamp not null,
userId int4 not null,
primary key (dc_id)
);

create table as_message (
dc_id int8 not null,
actionId varchar(64),
createdOn timestamp not null,
info varchar(255),
dc_status int4 not null,
outputData varchar(4096),
responseData varchar(4096),
responseRequired boolean not null,
retrieved boolean not null,
signature bytea,
signed boolean not null,
device_dc_id int4,
operatorId int4,
policyAppId int4,
template_dc_id int4,
userId int4 not null,
primary key (dc_id)
);

create table as_policy (
dc_id int4 not null,
jsonPolicy varchar(4096),
dc_name varchar(255),
primary key (dc_id)
);

create table as_policy_app (
dc_id int4 not null,
authapp varchar(255),
dc_disabled boolean,
subId int4 not null,
subname varchar(255),
primary key (dc_id)
);

create table as_ref_cloudsafe_tag (
dc_id int4 not null,
tags_dc_id int4 not null,
primary key (dc_id, tags_dc_id)
);

create table as_userfingerprint (
policyAppId int4 not null,
userId int4 not null,
fingerprint varchar(255),
timeStamp timestamp,
primary key (policyAppId, userId)
);

create table as_version (
dc_id int4 not null,
clientType int4,
dc_disabled boolean,
downloadUrl varchar(255),
expiresOn timestamp,
informationUrl varchar(255),
jpaVersion int4 not null,
dc_name varchar(128),
testApp boolean,
as_version int4,
versionStr varchar(128),
user_dc_id int4,
primary key (dc_id)
);

alter table as_authApp
add constraint UK_AUTHAPP_NAME unique (dc_name);

alter table as_cloudsafe
add constraint UK_AS_CLOUDDATA unique (dc_name, owner, user_dc_id, device_dc_id, dc_parent_id, group_dc_id);

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
references core_user;

alter table as_app_policy_group
add constraint FK_REF_GROUP
foreign key (group_id)
references core_group;

alter table as_app_policy_group
add constraint FK_REF_APP_POLICY
foreign key (policyApp_id)
references as_policy_app;

alter table as_app_policy_group
add constraint FK_REF_POLICY
foreign key (policy_id)
references as_policy;

alter table as_cloudsafe
add constraint FK_AS_PROP_DEVICE
foreign key (device_dc_id)
references as_device;

alter table as_cloudsafe
add constraint FK_AS_PROP_GROUP
foreign key (group_dc_id)
references core_group;

alter table as_cloudsafe
add constraint FK_AS_PROP_USER_MODIFIED
foreign key (lastModifiedUser_dc_id)
references core_user;

alter table as_cloudsafe
add constraint FK_AS_PARENT_ID
foreign key (dc_parent_id)
references as_cloudsafe;

alter table as_cloudsafe
add constraint FK_AS_PROP_USER
foreign key (user_dc_id)
references core_user;

alter table as_cloudsafelimit
add constraint FK_CLOUDSAFE_LIMIT
foreign key (user_dc_id)
references core_user;

alter table as_cloudsafeshare
add constraint FK_AS_CD_SHARE
foreign key (cloudSafe_dc_id)
references as_cloudsafe
on delete cascade;

alter table as_cloudsafeshare
add constraint FK_AS_CD_GROUP
foreign key (group_dc_id)
references core_group;

alter table as_cloudsafeshare
add constraint FK_AS_CD_SHARE_USER
foreign key (user_dc_id)
references core_user;

alter table as_cloudsafethumbnail
add constraint FK_CLOUDSAFE_THUMBNAIL
foreign key (dc_id)
references as_cloudsafe;

alter table as_device
add constraint FK_APP_DEVICE_VERSION
foreign key (asVersion_dc_id)
references as_version;

alter table as_device
add constraint FK_APP_DEVICE_USER
foreign key (userId)
references core_user;

alter table as_fido_authenticator
add constraint FK_FIDO_USER
foreign key (userId)
references core_user;

alter table as_message
add constraint FK_APP_MSG_DEVICE
foreign key (device_dc_id)
references as_device;

alter table as_message
add constraint FK_MSG_OPERATOR
foreign key (operatorId)
references core_user;

alter table as_message
add constraint FK_MSG_POLICYAPP
foreign key (policyAppId)
references as_policy_app;

alter table as_message
add constraint FK_APP_MSG_TEMPLATE
foreign key (template_dc_id)
references core_template;

alter table as_message
add constraint FK_APP_MSG_USER
foreign key (userId)
references core_user;

alter table as_ref_cloudsafe_tag
add constraint FKtn5egj0ktr5del1n3rrrrvq3a
foreign key (tags_dc_id)
references as_cloudsafe_tag;

alter table as_ref_cloudsafe_tag
add constraint FK_CLOUDSAFE_TAG
foreign key (dc_id)
references as_cloudsafe;

alter table as_version
add constraint FK_APP_VERSION_USER
foreign key (user_dc_id)
references core_user;
