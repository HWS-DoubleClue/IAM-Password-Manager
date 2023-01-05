
create table appo_timeEvent (
id number(19,0) generated as identity,
tp_end timestamp,
tp_start timestamp,
userInfo long,
userappo number(10,0) not null,
timeEvent number(19,0) not null,
primary key (id)
);

create table appo_timetable (
dc_id number(19,0) generated as identity,
dc_description varchar2(255 char),
dc_enabled number(1,0),
dc_name varchar2(255 char),
slotTimeMinutes number(10,0) not null,
primary key (dc_id)
);

create table appo_timetableEvent (
dc_id number(19,0) generated as identity,
dc_end timestamp,
dc_start timestamp,
timeEvent number(19,0) not null,
primary key (dc_id)
);

alter table appo_timeEvent
add constraint FK_USER_APPOINTMENTS
foreign key (userappo)
references core_user;

alter table appo_timeEvent
add constraint FK_EVENT_TIMETABLE
foreign key (timeEvent)
references appo_timetableEvent;

alter table appo_timetableEvent
add constraint FK_EVENT_TIMETABLE
foreign key (timeEvent)
references appo_timetable;
