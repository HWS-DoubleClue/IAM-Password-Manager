create sequence up_apphubseq start 1 increment 1;

create table up_applicationhub (
up_id int4 not null,
application varchar(10000) not null,
logo bytea,
up_name varchar(255) not null,
primary key (up_id)
);

create table up_keepassentry (
dc_id varchar(255) not null,
application varchar(10000),
up_name varchar(255) not null,
appEntity int4,
primary key (dc_id)
);

alter table up_applicationhub
add constraint UK_APPHUB_NAME unique (up_name);

alter table up_keepassentry
add constraint FK_KEEPASS_APP
foreign key (appEntity)
references up_applicationhub;
