create table shifts_absence (
dc_id integer not null,
absenseType integer,
comment varchar(255),
currentDate date,
endDate date,
startDate date,
dc_user integer not null,
primary key (dc_id)
);

create table shifts_assignments (
dc_id integer not null,
duration float,
endDate time,
startDate time,
shifts_user_id integer,
shifts_shift_id integer,
primary key (dc_id)
);

create table shifts_entry (
dc_id integer not null,
endDate date,
resourcesRequired integer not null,
startDate date,
dc_team integer not null,
dc_type integer not null,
primary key (dc_id)
);

create table shifts_shift (
dc_id integer not null,
comment varchar(255),
ignoreWarning boolean,
shiftDate date,
dc_shiftentry integer not null,
primary key (dc_id)
);

create table shifts_ShiftUsers_Types (
shiftsuser_id integer not null,
shiftstype_id integer not null,
primary key (shiftsuser_id, shiftstype_id)
);

create table shifts_skills (
dc_id integer not null,
skillAbbriviation varchar(255),
skillName varchar(255),
primary key (dc_id)
);

create table shifts_team (
dc_id integer not null,
name varchar(255),
dc_style varchar(255),
primary key (dc_id)
);

create table shifts_type (
dc_id integer not null,
colorCode varchar(255),
duration float,
endTime time,
isOnCall boolean,
dc_name varchar(255),
startTime time,
dc_style varchar(255),
primary key (dc_id)
);

create table shifts_type_days (
ShiftsTypeEntity_dc_id integer not null,
workingDays varchar(255)
);

create table shifts_user (
dc_id integer not null,
availableOn date,
exitDate date,
dc_external boolean not null,
onCallAllowed boolean not null,
onCallNumber varchar(255),
dc_user integer not null,
primary key (dc_id)
);

create table shifts_users_skills (
shiftsuser_id integer not null,
shiftsskills_id integer not null,
primary key (shiftsuser_id, shiftsskills_id)
);
create unique index UK_SKILL_ABBR_UNIQUE on shifts_skills (skillAbbriviation);
create unique index UK_SKILL_NAME_UNIQUE on shifts_skills (skillName);

alter table shifts_absence
add constraint FK_SHIFTS_ABSENCE_USER
foreign key (dc_user)
references shifts_user;

alter table shifts_assignments
add constraint FKioty4hgetv6s1kr17rf0sa33u
foreign key (shifts_user_id)
references shifts_user;

alter table shifts_assignments
add constraint FK1kg14y068bxsqfehje32vqx9y
foreign key (shifts_shift_id)
references shifts_shift;

alter table shifts_entry
add constraint FK_SHIFTS_TEAM
foreign key (dc_team)
references shifts_team;

alter table shifts_entry
add constraint FK_SHIFTS_TYPE
foreign key (dc_type)
references shifts_type;

alter table shifts_shift
add constraint FK_SHIFTS_ENTRY_SHIFT
foreign key (dc_shiftentry)
references shifts_entry;

alter table shifts_ShiftUsers_Types
add constraint FK6oxqea21h6fulncfp55k59nl2
foreign key (shiftstype_id)
references shifts_type;

alter table shifts_ShiftUsers_Types
add constraint FKm7tuhl6stntbyv10ed887nl1l
foreign key (shiftsuser_id)
references shifts_user;

alter table shifts_type_days
add constraint FKb1keg8dwmv5i1dmo4hc5gxl4x
foreign key (ShiftsTypeEntity_dc_id)
references shifts_type;

alter table shifts_user
add constraint FK_SHIFTS_USER
foreign key (dc_user)
references core_user;

alter table shifts_users_skills
add constraint FK54uax9oju7j5l5a76qs607s4a
foreign key (shiftsskills_id)
references shifts_skills;

alter table shifts_users_skills
add constraint FKn3704yqfl7rghxok589294ejj
foreign key (shiftsuser_id)
references shifts_user;
