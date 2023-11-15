
create table dev_test (
dc_id int not null,
activate bit not null,
details varchar(1024),
devObjectTypes int,
dc_localdate date,
dc_localTime time,
dc_number int,
testUnit varchar(128) not null,
dc_datetime datetime2 not null,
primary key (dc_id)
);
