
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

alter table core_action
add constraint UK_SEM_ACTION unique (moduleId, subject, action);

alter table core_group
add constraint UK_APP_GROUP unique (dc_name);

alter table core_ldap
add constraint UK_LDAP_NAME unique (name);

alter table core_role
add constraint UK_ROLE_NAME unique (dc_name);

alter table core_user
add constraint UK_APP_USER unique (loginId);

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

alter table core_group
add constraint FK_GROUP_ROLE
foreign key (dc_role)
references core_role;

alter table core_group
add constraint FK_GROUP_LDAP
foreign key (dc_ldap)
references core_ldap;

alter table core_ref_user_group
add constraint FK_GROUP_USER
foreign key (user_id)
references core_user;

alter table core_ref_user_group
add constraint FK_USER_GROUP
foreign key (group_id)
references core_group;

alter table core_role_core_action
add constraint FK_ROLE_ACTION
foreign key (actions_dc_id)
references core_action;

alter table core_role_core_action
add constraint FKm8fcladhxpesfv9gs7r0leqqg
foreign key (core_role_dc_id)
references core_role;

alter table core_user
add constraint FK_USER_ROLE
foreign key (dc_role)
references core_role;

alter table core_user
add constraint FK_USER_EXTENSION
foreign key (userext)
references core_userext;

alter table core_user
add constraint FK_USER_LDAP
foreign key (dc_ldap)
references core_ldap;
