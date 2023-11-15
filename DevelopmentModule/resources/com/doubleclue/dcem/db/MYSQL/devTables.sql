
create table dev_test (
dc_id integer not null,
activate bit not null,
details varchar(1024),
devObjectTypes integer,
dc_localdate date,
dc_localTime time,
dc_number integer,
testUnit varchar(128) not null,
dc_datetime datetime not null,
primary key (dc_id)
) engine=InnoDB;
