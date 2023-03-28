
create table otp_token (
dc_id int not null,
counter int not null,
dc_disabled bit,
info varchar(255),
otpType int not null,
secretKey varbinary(MAX) not null,
serialNumber varchar(255) not null,
userId int,
primary key (dc_id)
);

alter table otp_token
add constraint UK_OTP_SERIAL unique (serialNumber);

alter table otp_token
add constraint FK_OTP_TOKEN_USER
foreign key (userId)
references core_user;
