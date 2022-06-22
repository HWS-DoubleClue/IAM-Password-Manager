create table up_apphub_group (
apphub_up_id number(10,0) not null,
group_dc_id number(10,0) not null
);

create table up_applicationhub (
up_id number(10,0) not null,
application long not null,
included number(1,0) not null,
logo blob,
up_name varchar2(255 char) not null,
primary key (up_id)
);

create table up_applicationhubdashboard (
up_application number(10,0) not null,
up_user number(10,0) not null,
up_index number(10,0),
primary key (up_application, up_user)
);

alter table up_applicationhub
add constraint UK_APPHUB_NAME unique (up_name);

alter table up_apphub_group
add constraint FK_APPHUB_GROUP
foreign key (group_dc_id)
references core_group;

alter table up_apphub_group
add constraint FK1wkg76ssvji8w1pag3w0atcw4
foreign key (apphub_up_id)
references up_applicationhub;

alter table up_applicationhubdashboard
add constraint FK_REF_APPHUB
foreign key (up_application)
references up_applicationhub;

alter table up_applicationhubdashboard
add constraint FK_REF_USER
foreign key (up_user)
references core_user;
