
create table otp_token (
dc_id integer not null,
counter integer not null,
dc_disabled bit,
info varchar(255),
otpType integer not null,
secretKey mediumblob not null,
serialNumber varchar(255) not null,
userId integer,
primary key (dc_id)
) engine=InnoDB;

alter table otp_token
add constraint UK_OTP_SERIAL unique (serialNumber);

alter table otp_token
add constraint FK_OTP_TOKEN_USER
foreign key (userId)
references core_user (dc_id);
