create sequence up_apphubseq start with 1 increment by 4;

create table up_applicationhub (
up_id integer not null,
application varchar(10000) not null,
logo varchar(42000) for bit data,
up_name varchar(255) not null,
primary key (up_id)
);

create table up_keepassentry (
dc_id varchar(255) not null,
application varchar(10000),
up_name varchar(255) not null,
appEntity integer,
primary key (dc_id)
);
create unique index UK_APPHUB_NAME on up_applicationhub (up_name);

alter table up_keepassentry
add constraint FK_KEEPASS_APP
foreign key (appEntity)
references up_applicationhub;
