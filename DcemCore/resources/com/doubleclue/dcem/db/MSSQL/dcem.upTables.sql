create table up_apphubseq (
next_val bigint
);

insert into up_apphubseq values ( 1 );

create table up_applicationhub (
up_id int not null,
application varchar(MAX) not null,
logo varbinary(MAX),
up_name varchar(255) not null,
primary key (up_id)
);

create table up_keepassentry (
dc_id varchar(255) not null,
application varchar(MAX),
up_name varchar(255) not null,
appEntity int,
primary key (dc_id)
);

alter table up_applicationhub
add constraint UK_APPHUB_NAME unique (up_name);

alter table up_keepassentry
add constraint FK_KEEPASS_APP
foreign key (appEntity)
references up_applicationhub;
