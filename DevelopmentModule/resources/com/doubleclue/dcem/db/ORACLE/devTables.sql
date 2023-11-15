
create table dev_test (
dc_id number(10,0) not null,
activate number(1,0) not null,
details varchar2(1024 char),
devObjectTypes number(10,0),
dc_localdate date,
dc_localTime date,
dc_number number(10,0),
testUnit varchar2(128 char) not null,
dc_datetime timestamp not null,
primary key (dc_id)
);
