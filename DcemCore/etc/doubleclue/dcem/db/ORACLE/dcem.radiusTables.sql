create table radius_client (
dc_id number(10,0) not null,
ignoreUsersPassword number(1,0) not null,
ipNumber varchar2(255 char) not null,
name varchar2(255 char) not null,
sharedSecret long raw not null,
useChallenge number(1,0) not null,
primary key (dc_id)
);

create table radius_report (
dc_id number(10,0) not null,
action number(10,0),
details varchar2(1024 char),
error number(1,0) not null,
nasClientName varchar2(128 char),
dc_time timestamp not null,
primary key (dc_id)
);

alter table radius_client
add constraint UK_RADIUS_IPNUMBER unique (ipNumber);
