create table up_apphub_group (
apphub_up_id int not null,
group_dc_id int not null
);

create table up_apphubseq (
next_val bigint
);

insert into up_apphubseq values ( 1 );

create table up_applicationhub (
up_id int not null,
application varchar(MAX) not null,
included bit not null,
logo varbinary(MAX),
up_name varchar(255) not null,
primary key (up_id)
);

create table up_applicationhubdashboard (
up_application int not null,
up_user int not null,
up_index int,
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
