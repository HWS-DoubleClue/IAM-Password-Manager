create sequence up_apphubseq start with 1 increment by  1;

create table up_applicationhub (
up_id number(10,0) not null,
application long not null,
logo blob,
up_name varchar2(255 char) not null,
primary key (up_id)
);

create table up_keepassentry (
dc_id varchar2(255 char) not null,
application long,
up_name varchar2(255 char) not null,
appEntity number(10,0),
primary key (dc_id)
);

alter table up_applicationhub
add constraint UK_APPHUB_NAME unique (up_name);

alter table up_keepassentry
add constraint FK_KEEPASS_APP
foreign key (appEntity)
references up_applicationhub;
