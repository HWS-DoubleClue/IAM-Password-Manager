
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

alter table otp_token
add constraint UK_OTP_SERIAL unique (serialNumber);

alter table otp_token
add constraint FK_OTP_TOKEN_USER
foreign key (userId)
references core_user;
