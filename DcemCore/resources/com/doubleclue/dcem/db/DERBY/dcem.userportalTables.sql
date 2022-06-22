create table up_apphub_group (
apphub_up_id integer not null,
group_dc_id integer not null
);

create table up_applicationhub (
up_id integer not null,
application varchar(10000) not null,
included boolean not null,
logo blob,
up_name varchar(255) not null,
primary key (up_id)
);

create table up_applicationhubdashboard (
up_application integer not null,
up_user integer not null,
up_index integer,
primary key (up_application, up_user)
);
create unique index UK_APPHUB_NAME on up_applicationhub (up_name);

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
