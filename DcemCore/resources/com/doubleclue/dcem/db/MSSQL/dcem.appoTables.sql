
create table appo_timeEvent (
id bigint identity not null,
tp_end datetime2,
tp_start datetime2,
userInfo varchar(4096),
userappo int not null,
timeEvent bigint not null,
primary key (id)
);

create table appo_timetable (
dc_id bigint identity not null,
dc_description varchar(255),
dc_enabled bit,
dc_name varchar(255),
slotTimeMinutes int not null,
primary key (dc_id)
);

create table appo_timetableEvent (
dc_id bigint identity not null,
dc_end datetime2,
dc_start datetime2,
timeEvent bigint not null,
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
