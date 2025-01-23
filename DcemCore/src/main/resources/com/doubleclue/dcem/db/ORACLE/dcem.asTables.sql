
create table as_activationcode (
dc_id number(10,0) not null,
activationCode long raw not null,
createdOn timestamp not null,
info varchar2(255 char),
validTill timestamp not null,
userId number(10,0) not null,
primary key (dc_id)
);

create table as_app_policy_group (
dc_id number(10,0) not null,
dc_priority number(10,0),
group_id number(10,0),
policyApp_id number(10,0) not null,
policy_id number(10,0),
primary key (dc_id)
);

create table as_authApp (
dc_id number(10,0) not null,
disabled number(1,0) not null,
dc_name varchar2(64 char),
retryCounter number(10,0) not null,
sharedKey long raw,
primary key (dc_id)
);

create table as_cloudsafe (
dc_id number(10,0) not null,
dcemMediaType number(10,0),
discardAfter timestamp,
dc_info varchar2(255 char),
dc_is_folder number(1,0) not null,
dc_gcm number(1,0) not null,
lastModified timestamp,
dc_length number(19,0),
dc_name varchar2(255 char) not null,
options varchar2(255 char),
owner number(10,0),
recycled number(1,0) not null,
dc_salt blob,
textExtract long raw,
text_length number(19,0),
device_dc_id number(10,0) not null,
group_dc_id number(10,0),
lastModifiedUser_dc_id number(10,0),
dc_parent_id number(10,0),
user_dc_id number(10,0) not null,
primary key (dc_id)
);

create table as_cloudsafe_tag (
dc_id number(10,0) generated as identity,
dc_color varchar2(64 char) not null,
dc_name varchar2(255 char) not null,
primary key (dc_id)
);

create table as_cloudsafecontent (
cloudDataEntity_dc_id number(10,0) not null,
content blob,
primary key (cloudDataEntity_dc_id)
);

create table as_cloudsafelimit (
expiry_date timestamp,
dc_limit number(19,0) not null,
ps_enabled number(1,0) not null,
dc_used number(19,0) not null,
user_dc_id number(10,0) not null,
primary key (user_dc_id)
);

create table as_cloudsafeshare (
dc_id number(10,0) not null,
restrictDownload number(1,0),
writeAccess number(1,0),
cloudSafe_dc_id number(10,0),
group_dc_id number(10,0),
user_dc_id number(10,0),
primary key (dc_id)
);

create table as_cloudsafethumbnail (
dc_id number(10,0) not null,
thumbnail blob,
primary key (dc_id)
);

create table as_device (
dc_id number(10,0) not null,
appOsVersion varchar2(255 char),
deviceHash blob,
deviceKey long raw,
lastLogin timestamp,
locale varchar2(2 char),
manufacture varchar2(255 char),
name varchar2(64 char),
nodeId number(10,0),
offlineCounter number(10,0) not null,
offlineKey long raw,
publicKey blob,
retryCounter number(10,0) not null,
risks varchar2(255 char),
dc_state number(10,0) not null,
dc_status number(10,0) not null,
udid blob,
asVersion_dc_id number(10,0),
userId number(10,0) not null,
primary key (dc_id)
);

create table as_fido_authenticator (
dc_id number(10,0) not null,
credentialId varchar2(255 char) not null,
display_name varchar2(255 char) not null,
lastUsed timestamp not null,
passwordless number(1,0) not null,
publicKey blob not null,
registeredOn timestamp not null,
userId number(10,0) not null,
primary key (dc_id)
);

create table as_message (
dc_id number(19,0) not null,
actionId varchar2(64 char),
createdOn timestamp not null,
info varchar2(255 char),
dc_status number(10,0) not null,
outputData long,
responseData long,
responseRequired number(1,0) not null,
retrieved number(1,0) not null,
signature blob,
signed number(1,0) not null,
device_dc_id number(10,0),
operatorId number(10,0),
policyAppId number(10,0),
template_dc_id number(10,0),
userId number(10,0) not null,
primary key (dc_id)
);

create table as_policy (
dc_id number(10,0) not null,
jsonPolicy long,
dc_name varchar2(255 char),
primary key (dc_id)
);

create table as_policy_app (
dc_id number(10,0) not null,
authapp varchar2(255 char),
dc_disabled number(1,0),
subId number(10,0) not null,
subname varchar2(255 char),
primary key (dc_id)
);

create table as_ref_cloudsafe_tag (
dc_id number(10,0) not null,
tags_dc_id number(10,0) not null,
primary key (dc_id, tags_dc_id)
);

create table as_userfingerprint (
policyAppId number(10,0) not null,
userId number(10,0) not null,
fingerprint varchar2(255 char),
timeStamp timestamp,
primary key (policyAppId, userId)
);

create table as_version (
dc_id number(10,0) not null,
clientType number(10,0),
dc_disabled number(1,0),
downloadUrl varchar2(255 char),
expiresOn timestamp,
informationUrl varchar2(255 char),
jpaVersion number(10,0) not null,
dc_name varchar2(128 char),
testApp number(1,0),
as_version number(10,0),
versionStr varchar2(128 char),
user_dc_id number(10,0),
primary key (dc_id)
);

alter table as_authApp
add constraint UK_AUTHAPP_NAME unique (dc_name);

alter table as_cloudsafe
add constraint UK_AS_CLOUDDATA unique (dc_name, owner, user_dc_id, device_dc_id, dc_parent_id, group_dc_id);

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
