
create table appo_timeEvent (
id  bigserial not null,
tp_end timestamp,
tp_start timestamp,
userInfo varchar(4096),
userappo int4 not null,
timeEvent int8 not null,
primary key (id)
);

create table appo_timetable (
dc_id  bigserial not null,
dc_description varchar(255),
dc_enabled boolean,
dc_name varchar(255),
slotTimeMinutes int4 not null,
primary key (dc_id)
);

create table appo_timetableEvent (
dc_id  bigserial not null,
dc_end timestamp,
dc_start timestamp,
timeEvent int8 not null,
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
