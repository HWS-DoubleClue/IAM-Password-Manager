
create table otp_token (
dc_id number(10,0) not null,
counter number(10,0) not null,
dc_disabled number(1,0),
info varchar2(255 char),
otpType number(10,0) not null,
secretKey long raw not null,
serialNumber varchar2(255 char) not null,
userId number(10,0),
primary key (dc_id)
);

alter table otp_token
add constraint UK_OTP_SERIAL unique (serialNumber);

alter table otp_token
add constraint FK_OTP_TOKEN_USER
foreign key (userId)
references core_user;
