create table radius_client (
dc_id int not null,
ignoreUsersPassword bit not null,
ipNumber varchar(255) not null,
name varchar(255) not null,
settingsJson varchar(4096),
sharedSecret varbinary(MAX) not null,
useChallenge bit not null,
primary key (dc_id)
);

create table radius_report (
dc_id int not null,
action int,
details varchar(1024),
error bit not null,
nasClientName varchar(128),
dc_time datetime2 not null,
primary key (dc_id)
);

alter table radius_client
add constraint UK_RADIUS_IPNUMBER unique (ipNumber);
