create table radius_client (
dc_id integer not null,
ignoreUsersPassword boolean not null,
ipNumber varchar(255) not null,
name varchar(255) not null,
sharedSecret long varchar for bit data not null,
useChallenge boolean not null,
primary key (dc_id)
);

create table radius_report (
dc_id integer not null,
action integer,
details varchar(1024),
error boolean not null,
nasClientName varchar(128),
dc_time timestamp not null,
primary key (dc_id)
);
create unique index UK_RADIUS_IPNUMBER on radius_client (ipNumber);
