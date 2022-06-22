create table shifts_absence (
dc_id number(10,0) not null,
absenseType number(10,0),
comment varchar2(255 char),
currentDate date,
endDate date,
startDate date,
dc_user number(10,0) not null,
primary key (dc_id)
);

create table shifts_assignments (
dc_id number(10,0) not null,
duration float,
endDate date,
startDate date,
shifts_user_id number(10,0),
shifts_shift_id number(10,0),
primary key (dc_id)
);

create table shifts_entry (
dc_id number(10,0) not null,
endDate date,
resourcesRequired number(10,0) not null,
startDate date,
dc_team number(10,0) not null,
dc_type number(10,0) not null,
primary key (dc_id)
);

create table shifts_shift (
dc_id number(10,0) not null,
comment varchar2(255 char),
ignoreWarning number(1,0),
shiftDate date,
dc_shiftentry number(10,0) not null,
primary key (dc_id)
);

create table shifts_ShiftUsers_Types (
shiftsuser_id number(10,0) not null,
shiftstype_id number(10,0) not null,
primary key (shiftsuser_id, shiftstype_id)
);

create table shifts_skills (
dc_id number(10,0) not null,
skillAbbriviation varchar2(255 char),
skillName varchar2(255 char),
primary key (dc_id)
);

create table shifts_team (
dc_id number(10,0) not null,
name varchar2(255 char),
dc_style varchar2(255 char),
primary key (dc_id)
);

create table shifts_type (
dc_id number(10,0) not null,
colorCode varchar2(255 char),
duration float,
endTime date,
isOnCall number(1,0),
dc_name varchar2(255 char),
startTime date,
dc_style varchar2(255 char),
primary key (dc_id)
);

create table shifts_type_days (
ShiftsTypeEntity_dc_id number(10,0) not null,
workingDays varchar2(255 char)
);

create table shifts_user (
dc_id number(10,0) not null,
availableOn date,
exitDate date,
dc_external number(1,0) not null,
onCallAllowed number(1,0) not null,
onCallNumber varchar2(255 char),
dc_user number(10,0) not null,
primary key (dc_id)
);

create table shifts_users_skills (
shiftsuser_id number(10,0) not null,
shiftsskills_id number(10,0) not null,
primary key (shiftsuser_id, shiftsskills_id)
);

alter table shifts_skills
add constraint UK_SKILL_ABBR_UNIQUE unique (skillAbbriviation);

alter table shifts_skills
add constraint UK_SKILL_NAME_UNIQUE unique (skillName);

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
