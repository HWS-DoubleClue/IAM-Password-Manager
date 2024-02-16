
create table radius_client (
dc_id int4 not null,
ignoreUsersPassword boolean not null,
ipNumber varchar(255) not null,
name varchar(255) not null,
settingsJson varchar(4096),
sharedSecret bytea not null,
useChallenge boolean not null,
primary key (dc_id)
);

create table radius_report (
dc_id int4 not null,
action int4,
details varchar(1024),
error boolean not null,
nasClientName varchar(128),
dc_time timestamp not null,
primary key (dc_id)
);

alter table radius_client
add constraint UK_RADIUS_IPNUMBER unique (ipNumber);
