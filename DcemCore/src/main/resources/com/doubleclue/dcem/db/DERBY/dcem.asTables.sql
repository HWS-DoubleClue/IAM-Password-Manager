
create table as_activationcode (
dc_id integer not null,
activationCode long varchar for bit data not null,
createdOn timestamp not null,
info varchar(255),
validTill timestamp not null,
userId integer not null,
primary key (dc_id)
);

create table as_app_policy_group (
dc_id integer not null,
dc_priority integer,
group_id integer,
policyApp_id integer not null,
policy_id integer,
primary key (dc_id)
);

create table as_authApp (
dc_id integer not null,
disabled boolean not null,
dc_name varchar(64),
retryCounter integer not null,
sharedKey long varchar for bit data,
primary key (dc_id)
);

create table as_cloudsafe (
dc_id integer not null,
discardAfter timestamp,
dc_info varchar(255),
dc_is_folder boolean not null,
dc_gcm boolean not null,
lastModified timestamp,
dc_length bigint,
dc_name varchar(255) not null,
options varchar(255),
owner integer,
recycled boolean not null,
dc_salt varchar(32) for bit data,
device_dc_id integer not null,
group_dc_id integer,
lastModifiedUser_dc_id integer,
dc_parent_id integer,
user_dc_id integer not null,
primary key (dc_id)
);

create table as_cloudsafecontent (
cloudDataEntity_dc_id integer not null,
content blob,
primary key (cloudDataEntity_dc_id)
);

create table as_cloudsafelimit (
expiry_date timestamp,
dc_limit bigint not null,
ps_enabled boolean not null,
dc_used bigint not null,
user_dc_id integer not null,
primary key (user_dc_id)
);

create table as_cloudsafeshare (
dc_id integer not null,
restrictDownload boolean,
writeAccess boolean,
cloudSafe_dc_id integer,
group_dc_id integer,
user_dc_id integer,
primary key (dc_id)
);

create table as_device (
dc_id integer not null,
appOsVersion varchar(255),
deviceHash varchar(255) for bit data,
deviceKey long varchar for bit data,
lastLogin timestamp,
locale varchar(2),
manufacture varchar(255),
name varchar(64),
nodeId integer,
offlineCounter integer not null,
offlineKey long varchar for bit data,
publicKey varchar(1024) for bit data,
retryCounter integer not null,
risks varchar(255),
dc_state integer not null,
dc_status integer not null,
udid varchar(255) for bit data,
asVersion_dc_id integer,
userId integer not null,
primary key (dc_id)
);

create table as_fido_authenticator (
dc_id integer not null,
credentialId varchar(255) not null,
display_name varchar(255) not null,
lastUsed timestamp not null,
passwordless boolean not null,
publicKey varchar(1024) for bit data not null,
registeredOn timestamp not null,
userId integer not null,
primary key (dc_id)
);

create table as_message (
dc_id bigint not null,
actionId varchar(64),
createdOn timestamp not null,
info varchar(255),
dc_status integer not null,
outputData varchar(4096),
responseData varchar(4096),
responseRequired boolean not null,
retrieved boolean not null,
signature varchar(255) for bit data,
signed boolean not null,
device_dc_id integer,
operatorId integer,
policyAppId integer,
template_dc_id integer,
userId integer not null,
primary key (dc_id)
);

create table as_policy (
dc_id integer not null,
jsonPolicy varchar(4096),
dc_name varchar(255),
primary key (dc_id)
);

create table as_policy_app (
dc_id integer not null,
authapp varchar(255),
dc_disabled boolean,
subId integer not null,
subname varchar(255),
primary key (dc_id)
);

create table as_userfingerprint (
policyAppId integer not null,
userId integer not null,
fingerprint varchar(255),
timeStamp timestamp,
primary key (policyAppId, userId)
);

create table as_version (
dc_id integer not null,
clientType integer,
dc_disabled boolean,
downloadUrl varchar(255),
expiresOn timestamp,
informationUrl varchar(255),
jpaVersion integer not null,
dc_name varchar(128),
testApp boolean,
as_version integer,
versionStr varchar(128),
user_dc_id integer,
primary key (dc_id)
);

create unique index UK_AUTHAPP_NAME on as_authApp (dc_name);

create unique index UK_AS_CLOUDDATA on as_cloudsafe (dc_name, owner, user_dc_id, device_dc_id, dc_parent_id, group_dc_id);

create index IDX_DEVICE_LAST_LOGIN on as_device (lastLogin, dc_state);

create index IDX_DEVICE_USER on as_device (userId);

create unique index UK_DEVICE_USER on as_device (userId, name);

create index FIDO_AUTH_CREDENTIAL_ID_INDEX on as_fido_authenticator (credentialId);

create unique index UK_USER_CREDENTIAL_ID on as_fido_authenticator (userId, credentialId);

create unique index UK_POLICY_NAME on as_policy (dc_name);

create unique index UK_POLICY_APP on as_policy_app (authapp, subId);

create unique index UK_VERSION_NAME_TYPE on as_version (dc_name, versionStr, clientType);

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

alter table as_version
add constraint FK_APP_VERSION_USER
foreign key (user_dc_id)
references core_user;
