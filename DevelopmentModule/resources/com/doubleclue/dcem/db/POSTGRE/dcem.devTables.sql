
create table dev_test (
dc_id int4 not null,
activate boolean not null,
details varchar(1024),
devObjectTypes int4,
dc_localdate date,
dc_localTime time,
dc_number int4,
testUnit varchar(128) not null,
dc_datetime timestamp not null,
primary key (dc_id)
);
