
create table otp_token (
dc_id int4 not null,
counter int4 not null,
dc_disabled boolean,
info varchar(255),
otpType int4 not null,
secretKey bytea not null,
serialNumber varchar(255) not null,
userId int4,
primary key (dc_id)
);

alter table core_action
add constraint UK_SEM_ACTION unique (moduleId, subject, action);

alter table core_ldap
add constraint UK_LDAP_NAME unique (name);

alter table core_role
add constraint UK_ROLE_NAME unique (dc_name);

alter table core_user
add constraint UK_APP_USER unique (loginId);

alter table otp_token
add constraint UK_OTP_SERIAL unique (serialNumber);

alter table core_role_core_action
add constraint FK_ROLE_ACTION
foreign key (actions_dc_id)
references core_action;

alter table core_role_core_action
add constraint FKm8fcladhxpesfv9gs7r0leqqg
foreign key (core_role_dc_id)
references core_role;

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

alter table otp_token
add constraint FK_OTP_TOKEN_USER
foreign key (userId)
references core_user;
