
create table otp_token (
dc_id integer not null,
counter integer not null,
dc_disabled boolean,
info varchar(255),
otpType integer not null,
secretKey long varchar for bit data not null,
serialNumber varchar(255) not null,
userId integer,
primary key (dc_id)
);
create unique index UK_SEM_ACTION on core_action (moduleId, subject, action);
create unique index UK_LDAP_NAME on core_ldap (name);
create unique index UK_ROLE_NAME on core_role (dc_name);
create unique index UK_APP_USER on core_user (loginId);
create unique index UK_OTP_SERIAL on otp_token (serialNumber);

alter table otp_token
add constraint FK_OTP_TOKEN_USER
foreign key (userId)
references core_user;
