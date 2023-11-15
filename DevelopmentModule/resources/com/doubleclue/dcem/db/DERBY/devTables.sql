
create table dev_test (
dc_id integer not null,
activate boolean not null,
details varchar(1024),
devObjectTypes integer,
dc_localdate date,
dc_localTime time,
dc_number integer,
testUnit varchar(128) not null,
dc_datetime timestamp not null,
primary key (dc_id)
);
