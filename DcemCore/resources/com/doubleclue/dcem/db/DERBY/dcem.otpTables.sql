
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

create unique index UK_OTP_SERIAL on otp_token (serialNumber);

alter table otp_token
add constraint FK_OTP_TOKEN_USER
foreign key (userId)
references core_user;
