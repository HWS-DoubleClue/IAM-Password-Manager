
create table appo_timeEvent (
id bigint not null auto_increment,
tp_end datetime,
tp_start datetime,
userInfo varchar(4096),
userappo integer not null,
timeEvent bigint not null,
primary key (id)
) engine=InnoDB;

create table appo_timetable (
dc_id bigint not null auto_increment,
dc_description varchar(255),
dc_enabled bit,
dc_name varchar(255),
slotTimeMinutes integer not null,
primary key (dc_id)
) engine=InnoDB;

create table appo_timetableEvent (
dc_id bigint not null auto_increment,
dc_end datetime,
dc_start datetime,
timeEvent bigint not null,
primary key (dc_id)
) engine=InnoDB;

alter table appo_timeEvent
add constraint FK_USER_APPOINTMENTS
foreign key (userappo)
references core_user (dc_id);

alter table appo_timeEvent
add constraint FK_EVENT_TIMETABLE
foreign key (timeEvent)
references appo_timetableEvent (dc_id);

alter table appo_timetableEvent
add constraint FK_EVENT_TIMETABLE
foreign key (timeEvent)
references appo_timetable (dc_id);
